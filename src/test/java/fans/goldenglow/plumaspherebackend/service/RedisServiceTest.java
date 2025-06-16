package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.config.EmbeddedRedisTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({EmbeddedRedisTestConfiguration.class})
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clean up Redis before each test - delete all keys
        Set<String> keys = redisTemplate.keys("*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void testSetAndGet() {
        // Given
        String key = "test-key";
        String value = "test-value";

        // When
        redisService.set(key, value);

        // Then
        assertThat(redisService.get(key)).isEqualTo(value);
    }

    @Test
    void testGetNonExistentKey() {
        // Given
        String key = "non-existent-key";

        // When
        String result = redisService.get(key);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testDelete() {
        // Given
        String key = "test-key";
        String value = "test-value";
        redisService.set(key, value);

        // When
        redisService.delete(key);

        // Then
        assertThat(redisService.get(key)).isNull();
    }

    @Test
    void testExists() {
        // Given
        String key = "test-key";
        String value = "test-value";

        // When
        redisService.set(key, value);

        // Then
        assertThat(redisService.exists(key)).isTrue();
        assertThat(redisService.exists("non-existent-key")).isFalse();
    }

    @Test
    void testAddToSet() {
        // Given
        String key = "test-set";
        String value = "set-value";

        // When
        redisService.addToSet(key, value);

        // Then
        Set<String> setMembers = redisService.getSetMembers(key);
        assertThat(setMembers).contains(value);
    }

    @Test
    void testRemoveFromSet() {
        // Given
        String key = "test-set";
        String value = "set-value";
        redisService.addToSet(key, value);

        // When
        redisService.removeFromSet(key, value);

        // Then
        Set<String> setMembers = redisService.getSetMembers(key);
        assertThat(setMembers).doesNotContain(value);
    }

    @Test
    void testGetSetMembers() {
        // Given
        String key = "test-set";
        String value1 = "value1";
        String value2 = "value2";

        // When
        redisService.addToSet(key, value1);
        redisService.addToSet(key, value2);

        // Then
        Set<String> setMembers = redisService.getSetMembers(key);
        assertThat(setMembers).containsExactlyInAnyOrder(value1, value2);
    }

    @Test
    void testExistsInSet() {
        // Given
        String key = "test-set";
        String value = "test-value";
        redisService.addToSet(key, value);

        // When & Then
        assertThat(redisService.existsInSet(key, value)).isTrue();
        assertThat(redisService.existsInSet(key, "non-existent")).isFalse();
    }

    @Test
    void testGetSetSize() {
        // Given
        String key = "test-set";
        String value1 = "value1";
        String value2 = "value2";

        // When
        redisService.addToSet(key, value1);
        redisService.addToSet(key, value2);

        // Then
        assertThat(redisService.getSetSize(key)).isEqualTo(2L);
    }

    @Test
    void testSetOperationsOnNonExistentSet() {
        // Given
        String key = "non-existent-set";

        // When & Then
        assertThat(redisService.getSetMembers(key)).isEmpty();
        assertThat(redisService.existsInSet(key, "value")).isFalse();
        assertThat(redisService.getSetSize(key)).isEqualTo(0L);
    }

    @Test
    void testGetKeys() {
        // Given
        redisService.set("key1", "value1");
        redisService.set("key2", "value2");
        redisService.set("other", "value");

        // When
        Set<String> keys = redisService.getKeys("key*");

        // Then
        assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
    }

    @Test
    void testMultipleOperations() {
        // Given
        String key1 = "key1";
        String key2 = "key2";
        String value1 = "value1";
        String value2 = "value2";

        // When
        redisService.set(key1, value1);
        redisService.addToSet(key2, value2);

        // Then
        assertThat(redisService.get(key1)).isEqualTo(value1);
        assertThat(redisService.existsInSet(key2, value2)).isTrue();

        // Clean up specific keys
        redisService.delete(key1);
        redisService.removeFromSet(key2, value2);

        // Verify cleanup
        assertThat(redisService.get(key1)).isNull();
        assertThat(redisService.existsInSet(key2, value2)).isFalse();
    }
}
