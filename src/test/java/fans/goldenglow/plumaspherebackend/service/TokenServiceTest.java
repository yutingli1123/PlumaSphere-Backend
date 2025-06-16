package fans.goldenglow.plumaspherebackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.TokenResponseDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private SecretService secretService;

    @InjectMocks
    private TokenService tokenService;

    private SecretKey testSecretKey;
    private Algorithm testAlgorithm;
    private User testUser;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Generate a test secret key
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256);
        testSecretKey = keyGen.generateKey();
        testAlgorithm = Algorithm.HMAC256(testSecretKey.getEncoded());

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(UserRoles.REGULAR);

        // Set configuration values
        ReflectionTestUtils.setField(tokenService, "JWT_ISSUER", "test-issuer");
        ReflectionTestUtils.setField(tokenService, "ACCESS_TOKEN_EXPIRATION", 60L);
        ReflectionTestUtils.setField(tokenService, "REFRESH_TOKEN_EXPIRATION", 1440L);

        // Initialize the algorithm in TokenService
        ReflectionTestUtils.setField(tokenService, "algorithm", testAlgorithm);
    }

    @Test
    void generateTokens_ShouldReturnValidTokenResponse_WhenValidInputProvided() {
        // Given
        Long userId = 1L;
        List<String> scopes = List.of("user");

        // When
        TokenResponseDto tokenResponse = tokenService.generateTokens(userId, scopes);

        // Then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getRefreshToken()).isNotNull();

        assertThat(tokenResponse.getAccessToken().getToken()).isNotEmpty();
        assertThat(tokenResponse.getRefreshToken().getToken()).isNotEmpty();

        assertThat(tokenResponse.getAccessToken().getExpiresAt()).isNotNull();
        assertThat(tokenResponse.getRefreshToken().getExpiresAt()).isNotNull();

        // Verify tokens are different
        assertThat(tokenResponse.getAccessToken().getToken())
                .isNotEqualTo(tokenResponse.getRefreshToken().getToken());
    }

    @Test
    void generateTokens_ShouldCreateTokensWithCorrectClaims() {
        // Given
        Long userId = 1L;
        List<String> scopes = List.of("admin", "user");

        // When
        TokenResponseDto tokenResponse = tokenService.generateTokens(userId, scopes);

        // Then
        String accessToken = tokenResponse.getAccessToken().getToken();
        String refreshToken = tokenResponse.getRefreshToken().getToken();

        // Verify access token claims
        var decodedAccessToken = JWT.require(testAlgorithm).build().verify(accessToken);
        assertThat(decodedAccessToken.getSubject()).isEqualTo("1");
        assertThat(decodedAccessToken.getIssuer()).isEqualTo("test-issuer");
        assertThat(decodedAccessToken.getClaim("scope").asString()).isEqualTo("admin user");

        // Verify refresh token claims
        var decodedRefreshToken = JWT.require(testAlgorithm).build().verify(refreshToken);
        assertThat(decodedRefreshToken.getSubject()).isEqualTo("1");
        assertThat(decodedRefreshToken.getIssuer()).isEqualTo("test-issuer");
        assertThat(decodedRefreshToken.getClaim("scope").asString()).isEqualTo("refresh_token");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 999L, Long.MAX_VALUE})
    void generateTokens_ShouldHandleDifferentUserIds(Long userId) {
        // Given
        List<String> scopes = List.of("user");

        // When
        TokenResponseDto tokenResponse = tokenService.generateTokens(userId, scopes);

        // Then
        assertThat(tokenResponse).isNotNull();

        String accessToken = tokenResponse.getAccessToken().getToken();
        var decodedToken = JWT.require(testAlgorithm).build().verify(accessToken);
        assertThat(decodedToken.getSubject()).isEqualTo(userId.toString());
    }

    @Test
    void generateTokens_ShouldHandleEmptyScopes() {
        // Given
        Long userId = 1L;
        List<String> emptyScopes = List.of();

        // When
        TokenResponseDto tokenResponse = tokenService.generateTokens(userId, emptyScopes);

        // Then
        assertThat(tokenResponse).isNotNull();

        String accessToken = tokenResponse.getAccessToken().getToken();
        var decodedToken = JWT.require(testAlgorithm).build().verify(accessToken);
        assertThat(decodedToken.getClaim("scope").asString()).isEmpty();
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshTokenProvided() {
        // Given
        String userId = "1";
        String refreshTokenValue = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(userId)
                .withClaim("scope", "refresh_token")
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign(testAlgorithm);

        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        TokenResponseDto tokenResponse = tokenService.refreshToken(refreshTokenValue);

        // Then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getRefreshToken()).isNotNull();

        verify(userService).findById(1L);
    }

    @Test
    void refreshToken_ShouldReturnNull_WhenInvalidRefreshTokenProvided() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        TokenResponseDto tokenResponse = tokenService.refreshToken(invalidToken);

        // Then
        assertThat(tokenResponse).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void refreshToken_ShouldReturnNull_WhenRefreshTokenHasWrongScope() {
        // Given
        String userId = "1";
        String tokenWithWrongScope = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(userId)
                .withClaim("scope", "user admin") // Wrong scope
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign(testAlgorithm);

        // When
        TokenResponseDto tokenResponse = tokenService.refreshToken(tokenWithWrongScope);

        // Then
        assertThat(tokenResponse).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void refreshToken_ShouldReturnNull_WhenUserNotFound() {
        // Given
        String userId = "999";
        String refreshTokenValue = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(userId)
                .withClaim("scope", "refresh_token")
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign(testAlgorithm);

        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When
        TokenResponseDto tokenResponse = tokenService.refreshToken(refreshTokenValue);

        // Then
        assertThat(tokenResponse).isNull();
        verify(userService).findById(999L);
    }

    @Test
    void refreshToken_ShouldReturnNull_WhenTokenHasNoScope() {
        // Given
        String userId = "1";
        String tokenWithoutScope = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(userId)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign(testAlgorithm);

        // When
        TokenResponseDto tokenResponse = tokenService.refreshToken(tokenWithoutScope);

        // Then
        assertThat(tokenResponse).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void refreshToken_ShouldReturnNull_WhenTokenIsExpired() {
        // Given
        String userId = "1";
        String expiredToken = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(userId)
                .withClaim("scope", "refresh_token")
                .withIssuedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS)) // Expired
                .sign(testAlgorithm);

        // When
        TokenResponseDto tokenResponse = tokenService.refreshToken(expiredToken);

        // Then
        assertThat(tokenResponse).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void extractUserIdFromJwt_ShouldReturnUserId_WhenValidJwtProvided() {
        // Given
        String userId = "123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId);

        JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
        when(jwtToken.getToken()).thenReturn(jwt);

        // When
        Long extractedUserId = tokenService.extractUserIdFromJwt(jwtToken);

        // Then
        assertThat(extractedUserId).isEqualTo(123L);
    }

    @Test
    void extractUserIdFromJwt_ShouldReturnNull_WhenSubjectIsNotNumeric() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("not-a-number");

        JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
        when(jwtToken.getToken()).thenReturn(jwt);

        // When
        Long extractedUserId = tokenService.extractUserIdFromJwt(jwtToken);

        // Then
        assertThat(extractedUserId).isNull();
    }

    @Test
    void extractUserIdFromJwt_ShouldReturnNull_WhenSubjectIsNull() {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(null);

        JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
        when(jwtToken.getToken()).thenReturn(jwt);

        // When
        Long extractedUserId = tokenService.extractUserIdFromJwt(jwtToken);

        // Then
        assertThat(extractedUserId).isNull();
    }

    @Test
    void extractUserIdFromJwt_ShouldReturnNull_WhenJwtTokenThrowsException() {
        // Given
        JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
        when(jwtToken.getToken()).thenThrow(new RuntimeException("JWT error"));

        // When
        Long extractedUserId = tokenService.extractUserIdFromJwt(jwtToken);

        // Then
        assertThat(extractedUserId).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "999", "9223372036854775807"})
        // Long.MAX_VALUE
    void extractUserIdFromJwt_ShouldHandleValidUserIds(String userIdString) {
        // Given
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userIdString);

        JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
        when(jwtToken.getToken()).thenReturn(jwt);

        // When
        Long extractedUserId = tokenService.extractUserIdFromJwt(jwtToken);

        // Then
        assertThat(extractedUserId).isEqualTo(Long.parseLong(userIdString));
    }

    @Test
    void refreshToken_ShouldGenerateTokensWithUserRole() {
        // Given
        String userId = "1";
        testUser.setRole(UserRoles.ADMIN);

        String refreshTokenValue = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(userId)
                .withClaim("scope", "refresh_token")
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign(testAlgorithm);

        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        TokenResponseDto tokenResponse = tokenService.refreshToken(refreshTokenValue);

        // Then
        assertThat(tokenResponse).isNotNull();

        String accessToken = tokenResponse.getAccessToken().getToken();
        var decodedToken = JWT.require(testAlgorithm).build().verify(accessToken);
        assertThat(decodedToken.getClaim("scope").asString()).isEqualTo("admin");

        verify(userService).findById(1L);
    }
}
