package fans.goldenglow.plumaspherebackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.TokenPairResponseDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing JWT tokens.
 * Provides methods to generate access and refresh tokens, refresh tokens, and extract user ID from JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final UserService userService;
    private final SecretService secretService;
    private Algorithm algorithm;

    // Configuration properties for JWT
    @Value("${config.jwt.iss}")
    private String JWT_ISSUER;
    @Value("${config.jwt.expiration.access_token}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${config.jwt.expiration.refresh_token}")
    private long REFRESH_TOKEN_EXPIRATION;

    /**
     * Initializes the JWT algorithm using the secret key from SecretService.
     * This method is called after the service is constructed.
     */
    @PostConstruct
    void init() {
        SecretKey secret = secretService.getSecret();
        this.algorithm = Algorithm.HMAC256(secret.getEncoded());
    }

    /**
     * Generates a JWT token with the specified user ID, expiration time, and scopes.
     *
     * @param userId            the ID of the user for whom the token is generated
     * @param expirationMinutes the expiration time in minutes; if 0, token does not expire
     * @param scopes            the list of scopes to include in the token
     * @return a TokenDetails object containing the generated token and its expiration time
     */
    private TokenPairResponseDto.TokenDetails generateToken(String userId, long expirationMinutes, List<String> scopes) {
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
            return new TokenPairResponseDto.TokenDetails(token, ZonedDateTime.ofInstant(expireAt, ZoneId.systemDefault()));
        } else {
            String token = JWT
                    .create()
                    .withIssuer(JWT_ISSUER)
                    .withIssuedAt(now)
                    .withSubject(userId)
                    .withClaim("scope", String.join(" ", scopes))
                    .sign(algorithm);
            return new TokenPairResponseDto.TokenDetails(token, null);
        }
    }

    /**
     * Generates a pair of access and refresh tokens for the specified user ID and scopes.
     *
     * @param userId the ID of the user for whom the tokens are generated
     * @param scopes the list of scopes to include in the tokens
     * @return a TokenPairResponseDto containing the access and refresh tokens
     */
    public TokenPairResponseDto generateTokens(Long userId, List<String> scopes) {
        String userIdStr = userId.toString();

        TokenPairResponseDto.TokenDetails accessToken = generateToken(userIdStr, ACCESS_TOKEN_EXPIRATION, scopes);
        TokenPairResponseDto.TokenDetails refreshToken = generateToken(userIdStr, REFRESH_TOKEN_EXPIRATION, List.of("refresh_token"));

        return new TokenPairResponseDto(accessToken, refreshToken);
    }

    /**
     * Refreshes the access token using the provided refresh token value.
     * Validates the refresh token and generates a new access token if valid.
     *
     * @param refreshTokenValue the value of the refresh token to validate and use for generating a new access token
     * @return a TokenPairResponseDto containing the new access token, or null if the refresh token is invalid
     */
    public TokenPairResponseDto refreshToken(String refreshTokenValue) {
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

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param jwtToken the JWT authentication token from which to extract the user ID
     * @return the user ID as a Long, or null if extraction fails
     */
    public Long extractUserIdFromJwt(JwtAuthenticationToken jwtToken) {
        if (jwtToken == null || jwtToken.getToken() == null) {
            log.error("JWT token is null");
            return null;
        }
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
