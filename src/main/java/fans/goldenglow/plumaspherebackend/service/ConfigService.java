package fans.goldenglow.plumaspherebackend.service;

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
    public Optional<String> get(String configKey) {
        return configRepository.findByConfigKey(configKey).map(Config::getConfigValue);
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
    public void set(Config systemConfig) {
        Optional<Config> config = configRepository.findByConfigKey(systemConfig.getConfigKey());
        Config configEntity;
        if (config.isPresent()) {
            configEntity = config.get();
            configEntity.setConfigValue(systemConfig.getConfigValue());
        } else {
            configEntity = systemConfig;
        }

        configRepository.save(configEntity);
    }
}
