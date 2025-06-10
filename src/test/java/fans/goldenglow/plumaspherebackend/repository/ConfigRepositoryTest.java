package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.Config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ConfigRepositoryTest {
    private final ConfigRepository configRepository;

    @Autowired
    public ConfigRepositoryTest(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Test
    public void testFindByConfigKey() {
        String configKey = "configKey";
        String configValue = "configValue";
        boolean isPublic = true;
        Config config = new Config("configKey", "configValue", true);
        configRepository.save(config);

        Optional<Config> foundConfig = configRepository.findByConfigKey(configKey);
        assertThat(foundConfig)
                .isPresent()
                .get()
                .satisfies(c -> {
                    assertThat(c.getConfigKey()).isEqualTo(configKey);
                    assertThat(c.getConfigValue()).isEqualTo(configValue);
                    assertThat(c.getIsOpenToPublic()).isEqualTo(isPublic);
                });
    }

    @Test
    public void testFindNonExistConfigKey() {
        String nonExistentKey = "nonExistentKey";
        Optional<Config> foundConfig = configRepository.findByConfigKey(nonExistentKey);
        assertThat(foundConfig).isNotPresent();
    }
}
