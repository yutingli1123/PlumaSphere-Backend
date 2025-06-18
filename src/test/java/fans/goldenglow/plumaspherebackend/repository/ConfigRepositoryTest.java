package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ConfigRepository Tests")
class ConfigRepositoryTest {

    private static final String TEST_CONFIG_KEY = "configKey";
    private static final String TEST_CONFIG_VALUE = "configValue";
    private static final String ANOTHER_CONFIG_KEY = "anotherKey";
    private static final String ANOTHER_CONFIG_VALUE = "anotherValue";
    
    private final ConfigRepository configRepository;
    private final TestEntityManager entityManager;

    @Autowired
    public ConfigRepositoryTest(ConfigRepository configRepository, TestEntityManager entityManager) {
        this.configRepository = configRepository;
        this.entityManager = entityManager;
    }

    @Nested
    @DisplayName("Config Key Operations")
    class ConfigKeyOperationsTests {
        @Test
        @DisplayName("Should return config when config exists")
        void findByConfigKey_ShouldReturnConfig_WhenConfigExists() {
            // Given
            Config config = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
            entityManager.persistAndFlush(config);

            // When
            Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY);

            // Then
            assertThat(foundConfig)
                    .isPresent()
                    .get()
                    .satisfies(c -> {
                        assertThat(c.getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
                        assertThat(c.getConfigValue()).isEqualTo(TEST_CONFIG_VALUE);
                        assertThat(c.getIsOpenToPublic()).isTrue();
                        assertThat(c.getId()).isNotNull();
                    });
        }

        @Test
        @DisplayName("Should return empty when config does not exist")
        void findByConfigKey_ShouldReturnEmpty_WhenConfigDoesNotExist() {
            // When
            Optional<Config> foundConfig = configRepository.findByConfigKey("nonExistentKey");

            // Then
            assertThat(foundConfig).isEmpty();
        }

        @Test
        @DisplayName("Should return private config when config is private")
        void findByConfigKey_ShouldReturnPrivateConfig_WhenConfigIsPrivate() {
            // Given
            Config privateConfig = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, false);
            entityManager.persistAndFlush(privateConfig);

            // When
            Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY);

            // Then
            assertThat(foundConfig)
                    .isPresent()
                    .get()
                    .satisfies(c -> {
                        assertThat(c.getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
                        assertThat(c.getConfigValue()).isEqualTo(TEST_CONFIG_VALUE);
                        assertThat(c.getIsOpenToPublic()).isFalse();
                    });
        }

        @Test
        @DisplayName("Should return correct config when multiple configs exist")
        void findByConfigKey_ShouldReturnCorrectConfig_WhenMultipleConfigsExist() {
            // Given
            Config config1 = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
            Config config2 = new Config(ANOTHER_CONFIG_KEY, ANOTHER_CONFIG_VALUE, false);
            entityManager.persistAndFlush(config1);
            entityManager.persistAndFlush(config2);

            // When
            Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY);

            // Then
            assertThat(foundConfig)
                    .isPresent()
                    .get()
                    .satisfies(c -> {
                        assertThat(c.getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
                        assertThat(c.getConfigValue()).isEqualTo(TEST_CONFIG_VALUE);
                        assertThat(c.getIsOpenToPublic()).isTrue();
                    });
        }

        @Test
        @DisplayName("Should be case sensitive")
        void findByConfigKey_ShouldBeCaseSensitive() {
            // Given
            Config config = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
            entityManager.persistAndFlush(config);

            // When
            Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY.toUpperCase());

            // Then
            assertThat(foundConfig).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty key")
        void findByConfigKey_ShouldHandleEmptyKey() {
            // When
            Optional<Config> foundConfig = configRepository.findByConfigKey("");

            // Then
            assertThat(foundConfig).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty value")
        void findByConfigKey_ShouldHandleEmptyValue() {
            // Given - Test with empty but not null value
            Config config = new Config(TEST_CONFIG_KEY, " ", true); // Single space to satisfy @NotEmpty
            entityManager.persistAndFlush(config);

            // When
            Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY);

            // Then
            assertThat(foundConfig)
                    .isPresent()
                    .get()
                    .satisfies(c -> {
                        assertThat(c.getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
                        assertThat(c.getConfigValue()).isEqualTo(" ");
                        assertThat(c.getIsOpenToPublic()).isTrue();
                    });
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveTests {
        @Test
        @DisplayName("Should persist config with all properties")
        void save_ShouldPersistConfig_WithAllProperties() {
            // Given
            Config config = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);

            // When
            Config savedConfig = entityManager.persistAndFlush(config);

            // Then
            assertThat(savedConfig.getId()).isNotNull();
            assertThat(savedConfig.getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
            assertThat(savedConfig.getConfigValue()).isEqualTo(TEST_CONFIG_VALUE);
            assertThat(savedConfig.getIsOpenToPublic()).isTrue();

            // Verify it can be found
            Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY);
            assertThat(foundConfig).isPresent();
        }

        @Test
        @DisplayName("Should update config when config already exists")
        void save_ShouldUpdateConfig_WhenConfigAlreadyExists() {
            // Given
            Config originalConfig = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
            Config savedConfig = entityManager.persistAndFlush(originalConfig);

            // When
            savedConfig.setConfigValue("updatedValue");
            savedConfig.setIsOpenToPublic(false);
            Config updatedConfig = entityManager.persistAndFlush(savedConfig);

            // Then
            Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY);
            assertThat(foundConfig)
                    .isPresent()
                    .get()
                    .satisfies(c -> {
                        assertThat(c.getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
                        assertThat(c.getConfigValue()).isEqualTo("updatedValue");
                        assertThat(c.getIsOpenToPublic()).isFalse();
                        assertThat(c.getId()).isEqualTo(updatedConfig.getId());
                    });
        }
    }
}
