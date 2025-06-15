package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ConfigRepositoryTest {

    private static final String TEST_CONFIG_KEY = "configKey";
    private static final String TEST_CONFIG_VALUE = "configValue";
    private static final String ANOTHER_CONFIG_KEY = "anotherKey";
    private static final String ANOTHER_CONFIG_VALUE = "anotherValue";
    
    private final ConfigRepository configRepository;

    @Autowired
    public ConfigRepositoryTest(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Test
    void findByConfigKey_ShouldReturnConfig_WhenConfigExists() {
        // Given
        Config config = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
        configRepository.save(config);

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
    void findByConfigKey_ShouldReturnEmpty_WhenConfigDoesNotExist() {
        // When
        Optional<Config> foundConfig = configRepository.findByConfigKey("nonExistentKey");

        // Then
        assertThat(foundConfig).isEmpty();
    }

    @Test
    void findByConfigKey_ShouldReturnPrivateConfig_WhenConfigIsPrivate() {
        // Given
        Config privateConfig = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, false);
        configRepository.save(privateConfig);

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
    void findByConfigKey_ShouldReturnCorrectConfig_WhenMultipleConfigsExist() {
        // Given
        Config config1 = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
        Config config2 = new Config(ANOTHER_CONFIG_KEY, ANOTHER_CONFIG_VALUE, false);
        configRepository.save(config1);
        configRepository.save(config2);

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
    void findByConfigKey_ShouldBeCaseSensitive() {
        // Given
        Config config = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
        configRepository.save(config);

        // When
        Optional<Config> foundConfig = configRepository.findByConfigKey(TEST_CONFIG_KEY.toUpperCase());

        // Then
        assertThat(foundConfig).isEmpty();
    }

    @Test
    void save_ShouldPersistConfig_WithAllProperties() {
        // Given
        Config config = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);

        // When
        Config savedConfig = configRepository.save(config);

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
    void findByConfigKey_ShouldHandleEmptyKey() {
        // When
        Optional<Config> foundConfig = configRepository.findByConfigKey("");

        // Then
        assertThat(foundConfig).isEmpty();
    }

    @Test
    void findByConfigKey_ShouldHandleEmptyValue() {
        // Given - Test with empty but not null value
        Config config = new Config(TEST_CONFIG_KEY, " ", true); // Single space to satisfy @NotEmpty
        configRepository.save(config);

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

    @Test
    void save_ShouldUpdateConfig_WhenConfigAlreadyExists() {
        // Given
        Config originalConfig = new Config(TEST_CONFIG_KEY, TEST_CONFIG_VALUE, true);
        Config savedConfig = configRepository.save(originalConfig);

        // When
        savedConfig.setConfigValue("updatedValue");
        savedConfig.setIsOpenToPublic(false);
        Config updatedConfig = configRepository.save(savedConfig);

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
