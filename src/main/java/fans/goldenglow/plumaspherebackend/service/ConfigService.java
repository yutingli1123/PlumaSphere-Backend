package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.Config;
import fans.goldenglow.plumaspherebackend.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class ConfigService {
    private final SystemConfigRepository systemConfigRepository;

    @Autowired
    public ConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @Transactional
    public void set(Config systemConfig) {
        Optional<Config> config = systemConfigRepository.findByConfigKey(systemConfig.getConfigKey());
        Config configEntity;
        if (config.isPresent()) {
            configEntity = config.get();
            configEntity.setConfigValue(systemConfig.getConfigValue());
        } else {
            configEntity = systemConfig;
        }

        systemConfigRepository.save(configEntity);
    }

    @Transactional(readOnly = true)
    public Optional<String> get(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey).map(Config::getConfigValue);
    }
}
