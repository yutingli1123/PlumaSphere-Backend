package fans.goldenglow.plumaspherebackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecretServiceTest {

    private SecretService secretService;

    @BeforeEach
    void setUp() {
        secretService = new SecretService();
    }

    @Test
    void getSecret_ShouldReturnSameSecret_WhenCalledMultipleTimes() {
        // When
        SecretKey secret1 = secretService.getSecret();
        SecretKey secret2 = secretService.getSecret();

        // Then
        assertThat(secret1).isNotNull();
        assertThat(secret2).isNotNull();
        assertThat(secret1).isSameAs(secret2); // Should be the same instance
    }

    @Test
    void getSecret_ShouldReturnValidHmacSha256Key() {
        // When
        SecretKey secret = secretService.getSecret();

        // Then
        assertThat(secret).isNotNull();
        assertThat(secret.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(secret.getEncoded()).isNotNull();
        assertThat(secret.getEncoded().length).isEqualTo(32); // 256 bits = 32 bytes
    }

    @Test
    void getSecret_ShouldReturnDifferentKeys_ForDifferentInstances() {
        // Given
        SecretService secretService1 = new SecretService();
        SecretService secretService2 = new SecretService();

        // When
        SecretKey secret1 = secretService1.getSecret();
        SecretKey secret2 = secretService2.getSecret();

        // Then
        assertThat(secret1).isNotNull();
        assertThat(secret2).isNotNull();
        assertThat(secret1.getEncoded()).isNotEqualTo(secret2.getEncoded());
    }

    @Test
    void getSecret_ShouldGenerateSecretLazily() {
        // Given
        SecretService newSecretService = new SecretService();

        // When - First call should generate the secret
        SecretKey firstCall = newSecretService.getSecret();

        // Then
        assertThat(firstCall).isNotNull();

        // When - Second call should return the same secret
        SecretKey secondCall = newSecretService.getSecret();

        // Then
        assertThat(secondCall).isSameAs(firstCall);
    }

    @Test
    void getSecret_ShouldHandleConcurrentAccess() throws InterruptedException {
        // Given
        SecretService concurrentSecretService = new SecretService();
        SecretKey[] results = new SecretKey[10];
        Thread[] threads = new Thread[10];

        // When - Multiple threads call getSecret simultaneously
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> results[index] = concurrentSecretService.getSecret());
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - All threads should get the same secret instance
        SecretKey firstSecret = results[0];
        assertThat(firstSecret).isNotNull();

        for (int i = 1; i < 10; i++) {
            assertThat(results[i]).isSameAs(firstSecret);
        }
    }

    @Test
    void getSecret_ShouldGenerateValidKeyFormat() {
        // When
        SecretKey secret = secretService.getSecret();

        // Then
        assertThat(secret).isNotNull();
        assertThat(secret.getFormat()).isEqualTo("RAW");
        assertThat(secret.getEncoded()).isNotEmpty();
    }

    @Test
    void getSecret_ShouldMaintainConsistency_AcrossMultipleCalls() {
        // When
        SecretKey secret1 = secretService.getSecret();
        byte[] encoded1 = secret1.getEncoded();

        SecretKey secret2 = secretService.getSecret();
        byte[] encoded2 = secret2.getEncoded();

        SecretKey secret3 = secretService.getSecret();
        byte[] encoded3 = secret3.getEncoded();

        // Then
        assertThat(encoded1).isEqualTo(encoded2);
        assertThat(encoded2).isEqualTo(encoded3);
        assertThat(encoded1).isEqualTo(encoded3);
    }

    @Test
    void secretService_ShouldHandleRepeatedInstantiation() {
        // Given & When
        SecretService service1 = new SecretService();
        SecretService service2 = new SecretService();
        SecretService service3 = new SecretService();

        SecretKey key1 = service1.getSecret();
        SecretKey key2 = service2.getSecret();
        SecretKey key3 = service3.getSecret();

        // Then - Each service should have its own unique secret
        assertThat(key1.getEncoded()).isNotEqualTo(key2.getEncoded());
        assertThat(key2.getEncoded()).isNotEqualTo(key3.getEncoded());
        assertThat(key1.getEncoded()).isNotEqualTo(key3.getEncoded());

        // But each service should return the same key on multiple calls
        assertThat(service1.getSecret()).isSameAs(key1);
        assertThat(service2.getSecret()).isSameAs(key2);
        assertThat(service3.getSecret()).isSameAs(key3);
    }
}
