package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.StringDto;
import fans.goldenglow.plumaspherebackend.dto.TokenResponseDto;
import fans.goldenglow.plumaspherebackend.dto.UserLoginDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.PasswordService;
import fans.goldenglow.plumaspherebackend.service.TokenService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("LoginController Tests")
class LoginControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordService passwordService;
    @InjectMocks
    private LoginController loginController;
    private AutoCloseable mocks;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    // Helper for UserLoginDto
    private UserLoginDto createUserLoginDto(String username, String password) {
        UserLoginDto dto = org.mockito.Mockito.mock(UserLoginDto.class);
        when(dto.getUsername()).thenReturn(username);
        when(dto.getPassword()).thenReturn(password);
        return dto;
    }

    @Nested
    @DisplayName("login endpoint")
    class LoginEndpoint {
        @Test
        @DisplayName("Should return token when credentials are valid")
        void login_ShouldReturnToken_WhenCredentialsValid() {
            UserLoginDto loginDto = createUserLoginDto("user", "pass");
            User user = new User("user", "hashed", null);
            user.setId(1L);
            user.setRole(fans.goldenglow.plumaspherebackend.constant.UserRoles.REGULAR);
            when(userService.findByUsername("user")).thenReturn(Optional.of(user));
            when(passwordService.verifyPassword("pass", "hashed")).thenReturn(true);
            TokenResponseDto.TokenDetails access = new TokenResponseDto.TokenDetails("access", null);
            TokenResponseDto.TokenDetails refresh = new TokenResponseDto.TokenDetails("refresh", null);
            TokenResponseDto token = new TokenResponseDto(access, refresh);
            when(tokenService.generateTokens(eq(1L), anyList())).thenReturn(token);
            ResponseEntity<TokenResponseDto> response = loginController.login(loginDto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(token);
            verify(userService).save(any(User.class));
        }

        @Test
        @DisplayName("Should return UNAUTHORIZED when user not found")
        void login_ShouldReturnUnauthorized_WhenUserNotFound() {
            UserLoginDto loginDto = createUserLoginDto("nouser", "pass");
            when(userService.findByUsername("nouser")).thenReturn(Optional.empty());
            ResponseEntity<TokenResponseDto> response = loginController.login(loginDto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return FORBIDDEN when password is incorrect")
        void login_ShouldReturnForbidden_WhenPasswordIncorrect() {
            UserLoginDto loginDto = createUserLoginDto("user", "wrong");
            User user = new User("user", "hashed", null);
            user.setId(1L);
            user.setRole(fans.goldenglow.plumaspherebackend.constant.UserRoles.REGULAR);
            when(userService.findByUsername("user")).thenReturn(Optional.of(user));
            when(passwordService.verifyPassword("wrong", "hashed")).thenReturn(false);
            ResponseEntity<TokenResponseDto> response = loginController.login(loginDto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("refresh-token endpoint")
    class RefreshTokenEndpoint {
        @Test
        @DisplayName("Should return new token when refresh token is valid")
        void refreshToken_ShouldReturnNewToken_WhenValid() {
            StringDto dto = new StringDto("refresh");
            TokenResponseDto.TokenDetails access = new TokenResponseDto.TokenDetails("access", null);
            TokenResponseDto.TokenDetails refresh = new TokenResponseDto.TokenDetails("refresh", null);
            TokenResponseDto token = new TokenResponseDto(access, refresh);
            when(tokenService.refreshToken("refresh")).thenReturn(token);
            ResponseEntity<TokenResponseDto> response = loginController.refreshToken(dto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(token);
        }

        @Test
        @DisplayName("Should return UNAUTHORIZED when refresh token is invalid")
        void refreshToken_ShouldReturnUnauthorized_WhenInvalid() {
            StringDto dto = new StringDto("bad");
            when(tokenService.refreshToken("bad")).thenReturn(null);
            ResponseEntity<TokenResponseDto> response = loginController.refreshToken(dto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("get-identity endpoint")
    class GetIdentityEndpoint {
        @Test
        @DisplayName("Should create new user and return token")
        void getIdentity_ShouldCreateUserAndReturnToken() {
            when(userService.existByUsername(anyString())).thenReturn(false);
            User user = new User("random", "pass");
            user.setId(2L);
            when(passwordService.generateRandomPassword()).thenReturn("pass");
            doAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(2L);
                return null;
            }).when(userService).save(any(User.class));
            TokenResponseDto.TokenDetails access = new TokenResponseDto.TokenDetails("access", null);
            TokenResponseDto.TokenDetails refresh = new TokenResponseDto.TokenDetails("refresh", null);
            TokenResponseDto token = new TokenResponseDto(access, refresh);
            when(tokenService.generateTokens(eq(2L), anyList())).thenReturn(token);
            ResponseEntity<TokenResponseDto> response = loginController.getIdentity();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(token);
        }
    }

    @Nested
    @DisplayName("getIdentityFallback method")
    class GetIdentityFallbackMethod {
        @Test
        @DisplayName("Should return TOO_MANY_REQUESTS with message")
        void getIdentityFallback_ShouldReturnTooManyRequests() {
            ResponseEntity<String> response = loginController.getIdentityFallback(new RuntimeException());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getBody()).contains("API rate limit reached");
        }
    }
}
