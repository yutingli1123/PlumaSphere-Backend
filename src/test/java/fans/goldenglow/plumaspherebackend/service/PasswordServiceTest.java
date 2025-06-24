package fans.goldenglow.plumaspherebackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PasswordService Tests")
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Nested
    @DisplayName("Password Encoding")
    class PasswordEncoding {

        @Test
        @DisplayName("Should return encoded password when valid password provided")
        void encodePassword_ShouldReturnEncodedPassword_WhenValidPasswordProvided() {
            // Given
            String password = "testPassword123";

            // When
            String encodedPassword = passwordService.encodePassword(password);

            // Then
            assertThat(encodedPassword).isNotNull();
            assertThat(encodedPassword).isNotEmpty();
            assertThat(encodedPassword).isNotEqualTo(password);
            assertThat(encodedPassword).startsWith("$argon2id$");
        }

        @Test
        @DisplayName("Should return different hashes when same password encoded multiple times")
        void encodePassword_ShouldReturnDifferentHashes_WhenSamePasswordEncodedMultipleTimes() {
            // Given
            String password = "samePassword";

            // When
            String encodedPassword1 = passwordService.encodePassword(password);
            String encodedPassword2 = passwordService.encodePassword(password);

            // Then
            assertThat(encodedPassword1).isNotEqualTo(encodedPassword2);
            assertThat(encodedPassword1).startsWith("$argon2id$");
            assertThat(encodedPassword2).startsWith("$argon2id$");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "a", "shortPwd", "veryLongPasswordWithManyCharacters123456789"})
        @DisplayName("Should handle various password lengths")
        void encodePassword_ShouldHandleVariousPasswordLengths(String password) {
            // When
            String encodedPassword = passwordService.encodePassword(password);

            // Then
            assertThat(encodedPassword).isNotNull();
            assertThat(encodedPassword).isNotEmpty();
            assertThat(encodedPassword).startsWith("$argon2id$");
        }

        @Test
        @DisplayName("Should handle special characters")
        void encodePassword_ShouldHandleSpecialCharacters() {
            // Given
            String password = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";

            // When
            String encodedPassword = passwordService.encodePassword(password);

            // Then
            assertThat(encodedPassword).isNotNull();
            assertThat(encodedPassword).isNotEmpty();
            assertThat(encodedPassword).startsWith("$argon2id$");
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void encodePassword_ShouldHandleUnicodeCharacters() {
            // Given
            String password = "密码测试中文";

            // When
            String encodedPassword = passwordService.encodePassword(password);

            // Then
            assertThat(encodedPassword).isNotNull();
            assertThat(encodedPassword).isNotEmpty();
            assertThat(encodedPassword).startsWith("$argon2id$");
        }
    }

    @Nested
    @DisplayName("Password Verification")
    class PasswordVerification {

        @Test
        @DisplayName("Should return true when password matches")
        void verifyPassword_ShouldReturnTrue_WhenPasswordMatches() {
            // Given
            String password = "testPassword123";
            String encodedPassword = passwordService.encodePassword(password);

            // When
            boolean result = passwordService.verifyPassword(password, encodedPassword);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when password does not match")
        void verifyPassword_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
            // Given
            String password = "testPassword123";
            String wrongPassword = "wrongPassword";
            String encodedPassword = passwordService.encodePassword(password);

            // When
            boolean result = passwordService.verifyPassword(wrongPassword, encodedPassword);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when encoded password is invalid")
        void verifyPassword_ShouldReturnFalse_WhenEncodedPasswordIsInvalid() {
            // Given
            String password = "testPassword123";
            String invalidEncodedPassword = "invalidHash";

            // When
            boolean result = passwordService.verifyPassword(password, invalidEncodedPassword);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when encoded password is null")
        void verifyPassword_ShouldReturnFalse_WhenEncodedPasswordIsNull() {
            // Given
            String password = "testPassword123";

            // When
            boolean result = passwordService.verifyPassword(password, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should throw NPE when password is null")
        void verifyPassword_ShouldThrowNPE_WhenPasswordIsNull() {
            // Given
            String encodedPassword = passwordService.encodePassword("testPassword123");

            // When & Then - Argon2PasswordEncoder throws NPE for null password
            assertThrows(NullPointerException.class, () ->
                    passwordService.verifyPassword(null, encodedPassword)
            );
        }

        @Test
        @DisplayName("Should return false when both are null")
        void verifyPassword_ShouldReturnFalse_WhenBothAreNull() {
            // When
            boolean result = passwordService.verifyPassword(null, null);

            // Then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "a", "shortPwd", "veryLongPasswordWithManyCharacters123456789"})
        @DisplayName("Should work with various password lengths")
        void verifyPassword_ShouldWork_WithVariousPasswordLengths(String password) {
            // Given
            String encodedPassword = passwordService.encodePassword(password);

            // When
            boolean result = passwordService.verifyPassword(password, encodedPassword);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should work with special characters")
        void verifyPassword_ShouldWork_WithSpecialCharacters() {
            // Given
            String password = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
            String encodedPassword = passwordService.encodePassword(password);

            // When
            boolean result = passwordService.verifyPassword(password, encodedPassword);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should work with unicode characters")
        void verifyPassword_ShouldWork_WithUnicodeCharacters() {
            // Given
            String password = "密码测试中文";
            String encodedPassword = passwordService.encodePassword(password);

            // When
            boolean result = passwordService.verifyPassword(password, encodedPassword);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should be case sensitive")
        void verifyPassword_ShouldBeCaseSensitive() {
            // Given
            String password = "TestPassword123";
            String wrongCasePassword = "testpassword123";
            String encodedPassword = passwordService.encodePassword(password);

            // When
            boolean correctResult = passwordService.verifyPassword(password, encodedPassword);
            boolean wrongResult = passwordService.verifyPassword(wrongCasePassword, encodedPassword);

            // Then
            assertThat(correctResult).isTrue();
            assertThat(wrongResult).isFalse();
        }
    }

    @Nested
    @DisplayName("Random Password Generation")
    class RandomPasswordGeneration {

        @Test
        @DisplayName("Should return non-null encoded password")
        void generateRandomPassword_ShouldReturnNonNullEncodedPassword() {
            // When
            String randomPassword = passwordService.generateRandomPassword();

            // Then
            assertThat(randomPassword).isNotNull();
            assertThat(randomPassword).isNotEmpty();
            assertThat(randomPassword).startsWith("$argon2id$");
        }

        @Test
        @DisplayName("Should return different passwords when called multiple times")
        void generateRandomPassword_ShouldReturnDifferentPasswords_WhenCalledMultipleTimes() {
            // When
            String password1 = passwordService.generateRandomPassword();
            String password2 = passwordService.generateRandomPassword();
            String password3 = passwordService.generateRandomPassword();

            // Then
            assertThat(password1).isNotEqualTo(password2);
            assertThat(password2).isNotEqualTo(password3);
            assertThat(password1).isNotEqualTo(password3);
        }

        @Test
        @DisplayName("Should generate valid encoded passwords")
        void generateRandomPassword_ShouldGenerateValidEncodedPasswords() {
            // When
            String randomPassword1 = passwordService.generateRandomPassword();
            String randomPassword2 = passwordService.generateRandomPassword();

            // Then
            assertThat(randomPassword1).startsWith("$argon2id$");
            assertThat(randomPassword2).startsWith("$argon2id$");

            // Generated passwords are already encoded, so they should be different from any plain text
            assertThat(randomPassword1).hasSizeGreaterThan(80).hasSizeLessThan(120); // Argon2 hash length range
            assertThat(randomPassword2).hasSizeGreaterThan(80).hasSizeLessThan(120);
        }

        @Test
        @DisplayName("Should create multiple unique passwords")
        void generateRandomPassword_ShouldCreateMultipleUniquePasswords() {
            // Given
            int numberOfPasswords = 10;

            // When
            String[] passwords = new String[numberOfPasswords];
            for (int i = 0; i < numberOfPasswords; i++) {
                passwords[i] = passwordService.generateRandomPassword();
            }

            // Then
            for (int i = 0; i < numberOfPasswords; i++) {
                assertThat(passwords[i]).isNotNull();
                assertThat(passwords[i]).startsWith("$argon2id$");

                // Check uniqueness
                for (int j = i + 1; j < numberOfPasswords; j++) {
                    assertThat(passwords[i]).isNotEqualTo(passwords[j]);
                }
            }
        }
    }

    @Nested
    @DisplayName("Consistency Across Instances")
    class ConsistencyAcrossInstances {

        @Test
        @DisplayName("Should be consistent across multiple instances")
        void passwordService_ShouldBeConsistent_AcrossMultipleInstances() {
            // Given
            PasswordService anotherPasswordService = new PasswordService();
            String password = "testPassword123";

            // When
            String encodedPassword1 = passwordService.encodePassword(password);
            String encodedPassword2 = anotherPasswordService.encodePassword(password);

            // Then
            // Different instances should produce different hashes (due to salt)
            assertThat(encodedPassword1).isNotEqualTo(encodedPassword2);

            // But both should be able to verify the original password
            assertThat(passwordService.verifyPassword(password, encodedPassword1)).isTrue();
            assertThat(passwordService.verifyPassword(password, encodedPassword2)).isTrue();
            assertThat(anotherPasswordService.verifyPassword(password, encodedPassword1)).isTrue();
            assertThat(anotherPasswordService.verifyPassword(password, encodedPassword2)).isTrue();
        }
    }
}
