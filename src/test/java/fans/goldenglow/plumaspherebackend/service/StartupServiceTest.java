package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static fans.goldenglow.plumaspherebackend.constant.RedisKey.INITIALIZATION_CODE_KEY;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StartupService Tests")
class StartupServiceTest {
    @Mock
    private ConfigService configService;

    @Mock
    private RedisService redisService;

    private StartupService startupService;

    @BeforeEach
    void setUp() {
        startupService = new StartupService(configService, redisService);
    }

    @Nested
    @DisplayName("Initialization Logic")
    class InitializationLogic {
        @Test
        @DisplayName("Should not generate code when already initialized")
        void testInit_WhenAlreadyInitialized_ShouldNotGenerateCode() {
            // Given
            when(configService.get(ConfigField.INITIALIZED)).thenReturn(Optional.of("true"));

            // When
            startupService.init();

            // Then
            verify(configService).get(ConfigField.INITIALIZED);
            verify(redisService, never()).get(anyString());
            verify(redisService, never()).set(anyString(), anyString());
        }

        @Test
        @DisplayName("Should use existing code when not initialized and code exists")
        void testInit_WhenNotInitializedAndCodeExists_ShouldUseExistingCode() {
            // Given
            when(configService.get(ConfigField.INITIALIZED)).thenReturn(Optional.empty());
            when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn("123456");

            // When
            startupService.init();

            // Then
            verify(configService).get(ConfigField.INITIALIZED);
            verify(redisService).get(INITIALIZATION_CODE_KEY);
            verify(redisService, never()).set(anyString(), anyString());
        }

        @Test
        @DisplayName("Should generate new code when not initialized and no code exists")
        void testInit_WhenNotInitializedAndNoCode_ShouldGenerateNewCode() {
            // Given
            when(configService.get(ConfigField.INITIALIZED)).thenReturn(Optional.empty());
            when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn(null);

            // When
            startupService.init();

            // Then
            verify(configService).get(ConfigField.INITIALIZED);
            verify(redisService).get(INITIALIZATION_CODE_KEY);
            verify(redisService).set(eq(INITIALIZATION_CODE_KEY), anyString());
        }

        @Test
        @DisplayName("Should only check config on multiple calls when initialized")
        void testInit_MultipleCallsWhenInitialized_ShouldOnlyCheckConfig() {
            // Given
            when(configService.get(ConfigField.INITIALIZED)).thenReturn(Optional.of("true"));

            // When
            startupService.init();
            startupService.init();
            startupService.init();

            // Then
            verify(configService, times(3)).get(ConfigField.INITIALIZED);
            verify(redisService, never()).get(anyString());
            verify(redisService, never()).set(anyString(), anyString());
        }

        @Test
        @DisplayName("Should process each time on multiple calls when not initialized")
        void testInit_MultipleCallsWhenNotInitialized_ShouldProcessEachTime() {
            // Given
            when(configService.get(ConfigField.INITIALIZED)).thenReturn(Optional.empty());
            when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn("123456");

            // When
            startupService.init();
            startupService.init();

            // Then
            verify(configService, times(2)).get(ConfigField.INITIALIZED);
            verify(redisService, times(2)).get(INITIALIZATION_CODE_KEY);
        }

        @Test
        @DisplayName("Should generate numeric verification code")
        void testInit_VerificationCodeGeneration_ShouldBeNumeric() {
            // Given
            when(configService.get(ConfigField.INITIALIZED)).thenReturn(Optional.empty());
            when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn(null);

            // When
            startupService.init();

            // Then
            verify(redisService).set(eq(INITIALIZATION_CODE_KEY), argThat(code -> {
                // Verify the generated code is a 6-digit number
                return code != null && code.matches("\\d{6}") &&
                        Integer.parseInt(code) >= 100000 && Integer.parseInt(code) <= 999999;
            }));
        }

        @Test
        @DisplayName("Should check ConfigField.INITIALIZED correctly")
        void testInit_ConfigFieldInitializedCheck() {
            // Given
            when(configService.get(ConfigField.INITIALIZED)).thenReturn(Optional.of("any_value"));

            // When
            startupService.init();

            // Then
            verify(configService).get(ConfigField.INITIALIZED);
            // Should not proceed with initialization when already initialized
            verifyNoInteractions(redisService);
        }
    }
}
