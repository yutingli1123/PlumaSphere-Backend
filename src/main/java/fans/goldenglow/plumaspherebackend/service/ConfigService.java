package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.SystemConfig;
import fans.goldenglow.plumaspherebackend.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ConfigService {
    private final SystemConfigRepository systemConfigRepository;

    @Autowired
    public ConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    public Boolean set(SystemConfig systemConfig) {
        SystemConfig config = systemConfigRepository.findByConfigKey(systemConfig.getConfigKey()).orElse(null);
        if (config != null) {
            config.setConfigValue(systemConfig.getConfigValue());
        } else {
            config = systemConfig;
        }
        try {
            systemConfigRepository.save(config);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public Optional<String> get(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey).map(SystemConfig::getConfigValue);
    }
}
