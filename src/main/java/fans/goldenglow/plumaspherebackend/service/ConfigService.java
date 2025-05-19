package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.entity.Config;
import fans.goldenglow.plumaspherebackend.repository.ConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConfigService {
    private final ConfigRepository configRepository;

    @Autowired
    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public Optional<String> get(ConfigField configField) {
        return configRepository.findByConfigKey(configField.name().toLowerCase()).map(Config::getConfigValue);
    }

    @Transactional(readOnly = true)
    public List<Config> getAll() {
        return configRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Config> getAllPublic() {
        List<Config> configs = configRepository.findAll();
        return configs.stream().filter(Config::getIsOpenToPublic).toList();
    }

    @Transactional
    public void set(ConfigField configField, String value) {
        set(configField, value, false);
    }

    @Transactional
    public void set(ConfigField configField, String value, boolean isOpenToPublic) {
        Optional<Config> config = configRepository.findByConfigKey(configField.name().toLowerCase());
        Config configEntity;
        if (config.isPresent()) {
            configEntity = config.get();
            configEntity.setConfigValue(value);
        } else {
            configEntity = new Config(configField.name().toLowerCase(), value, isOpenToPublic);
        }
        configRepository.save(configEntity);

        if (configField != ConfigField.CONFIG_VERSION) {
            incrementConfigVersion();
        }
    }

    @Transactional
    protected void incrementConfigVersion() {
        Optional<Config> versionConfig = configRepository.findByConfigKey(ConfigField.CONFIG_VERSION.name().toLowerCase());
        long version = versionConfig.map(config -> {
            try {
                return Long.parseLong(config.getConfigValue());
            } catch (NumberFormatException e) {
                return 0L;
            }
        }).orElse(0L);
        version++;
        Config configEntity = versionConfig.orElseGet(() -> new Config(ConfigField.CONFIG_VERSION.name().toLowerCase(), "1", false));
        configEntity.setConfigValue(String.valueOf(version));
        configRepository.save(configEntity);
    }
}
