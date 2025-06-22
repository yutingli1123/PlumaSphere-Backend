package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.dto.InitDto;
import fans.goldenglow.plumaspherebackend.entity.Config;
import fans.goldenglow.plumaspherebackend.service.ConfigService;
import fans.goldenglow.plumaspherebackend.service.PasswordService;
import fans.goldenglow.plumaspherebackend.service.RedisService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SystemController Tests")
class SystemControllerTest {
    @Mock
    private ConfigService configService;
    @Mock
    private RedisService redisService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private UserService userService;
    @InjectMocks
    private SystemController systemController;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("GET /api/v1/status")
    class GetStatus {
        @Test
        @DisplayName("Should return all configs for admin")
        void getStatus_Admin() {
            JwtAuthenticationToken token = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
            when(token.getTokenAttributes()).thenReturn(Collections.singletonMap("scope", "admin"));
            List<Config> configs = List.of(new Config());
            when(configService.getAll()).thenReturn(configs);
            ResponseEntity<List<Config>> response = systemController.getStatus(token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(configs);
        }

        @Test
        @DisplayName("Should return public configs for non-admin")
        void getStatus_NonAdmin() {
            JwtAuthenticationToken token = org.mockito.Mockito.mock(JwtAuthenticationToken.class);
            when(token.getTokenAttributes()).thenReturn(Collections.singletonMap("scope", "user"));
            List<Config> configs = List.of(new Config());
            when(configService.getAllPublic()).thenReturn(configs);
            ResponseEntity<List<Config>> response = systemController.getStatus(token);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(configs);
        }

        @Test
        @DisplayName("Should return public configs if token is null")
        void getStatus_NullToken() {
            List<Config> configs = List.of(new Config());
            when(configService.getAllPublic()).thenReturn(configs);
            ResponseEntity<List<Config>> response = systemController.getStatus(null);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(configs);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/status/version")
    class GetStatusVersion {
        @Test
        @DisplayName("Should return config version if present")
        void getStatusVersion_Present() {
            when(configService.get(ConfigField.CONFIG_VERSION)).thenReturn(Optional.of("2"));
            ResponseEntity<String> response = systemController.getStatusVersion();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("2");
        }

        @Test
        @DisplayName("Should return default version if not present")
        void getStatusVersion_Default() {
            when(configService.get(ConfigField.CONFIG_VERSION)).thenReturn(Optional.empty());
            ResponseEntity<String> response = systemController.getStatusVersion();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("1");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/init")
    class InitSystem {
        @Test
        @DisplayName("Should return UNAUTHORIZED if verification code invalid")
        void initSystem_VerificationFail() {
            InitDto dto = mock(InitDto.class);
            when(dto.getVerificationCode()).thenReturn("wrong");
            when(configService.checkVerificationCode("wrong")).thenReturn(false);
            ResponseEntity<Void> response = systemController.initSystem(dto);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
