package fans.goldenglow.plumaspherebackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.TokenResponseDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final UserService userService;
    private final SecretService secretService;
    private Algorithm algorithm;
    @Value("${config.jwt.iss}")
    private String JWT_ISSUER;
    @Value("${config.jwt.expiration.access_token}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${config.jwt.expiration.refresh_token}")
    private long REFRESH_TOKEN_EXPIRATION;

    @PostConstruct
    private void init() {
        this.algorithm = Algorithm.HMAC256(secretService.getSecret().getEncoded());
    }

    private TokenResponseDto.TokenDetails generateToken(String userId, long expirationMinutes, List<String> scopes) {
        Instant now = Instant.now();
        if (expirationMinutes != 0) {
            Instant expireAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

            String token = JWT
                    .create()
                    .withIssuer(JWT_ISSUER)
                    .withIssuedAt(now)
                    .withExpiresAt(expireAt)
                    .withSubject(userId)
                    .withClaim("scope", String.join(" ", scopes))
                    .sign(algorithm);
            return new TokenResponseDto.TokenDetails(token, ZonedDateTime.ofInstant(expireAt, ZoneId.systemDefault()));
        } else {
            String token = JWT
                    .create()
                    .withIssuer(JWT_ISSUER)
                    .withIssuedAt(now)
                    .withSubject(userId)
                    .withClaim("scope", String.join(" ", scopes))
                    .sign(algorithm);
            return new TokenResponseDto.TokenDetails(token, null);
        }
    }

    public TokenResponseDto generateTokens(Long userId, List<String> scopes) {
        String userIdStr = userId.toString();

        TokenResponseDto.TokenDetails accessToken = generateToken(userIdStr, ACCESS_TOKEN_EXPIRATION, scopes);
        TokenResponseDto.TokenDetails refreshToken = generateToken(userIdStr, REFRESH_TOKEN_EXPIRATION, List.of("refresh_token"));

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public TokenResponseDto refreshToken(String refreshTokenValue) {
        try {
            JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer(JWT_ISSUER).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(refreshTokenValue);

            String scopeValue = decodedJWT.getClaim("scope").asString();
            if (scopeValue == null || !scopeValue.contains("refresh_token")) return null;

            String userId = decodedJWT.getSubject();

            Optional<User> user = userService.findById(Long.parseLong(userId));
            if (user.isEmpty()) return null;

            User userEntity = user.get();

            UserRoles userRole = userEntity.getRole();

            return generateTokens(Long.parseLong(userId), List.of(userRole.toString().toLowerCase()));
        } catch (Exception e) {
            log.error("Failed to verify refresh token");
            return null;
        }
    }

    public Long extractUserIdFromJwt(JwtAuthenticationToken jwtToken) {
        try {
            return Long.parseLong(jwtToken.getToken().getSubject());
        } catch (NumberFormatException e) {
            log.error("Error parsing user ID from JWT subject: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from JWT: {}", e.getMessage());
            return null;
        }
    }
}
