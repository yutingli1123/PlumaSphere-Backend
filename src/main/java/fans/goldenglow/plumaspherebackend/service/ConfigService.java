package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.entity.Config;
import fans.goldenglow.plumaspherebackend.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fans.goldenglow.plumaspherebackend.constant.RedisKey.INITIALIZATION_CODE_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigService {
    private final ConfigRepository configRepository;
    private final RedisService redisService;

    private static final Set<ConfigField> IMMUTABLE_CONFIG_FIELDS = Set.of(
            ConfigField.CONFIG_VERSION
    );

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
        if (configField == ConfigField.INITIALIZED) {
            Optional<Config> initializedConfig = configRepository.findByConfigKey(configField.name().toLowerCase());
            if (initializedConfig.isPresent()) {
                log.warn("System already initialized, cannot set INITIALIZED field again");
                return;
            }
        } else if (IMMUTABLE_CONFIG_FIELDS.contains(configField)) {
            log.warn("Attempted to modify immutable config field: {}", configField);
            return;
        }

        Optional<Config> config = configRepository.findByConfigKey(configField.name().toLowerCase());
        Config configEntity;
        if (config.isPresent()) {
            configEntity = config.get();
            configEntity.setConfigValue(value);
        } else {
            configEntity = new Config(configField.name().toLowerCase(), value, configField.isOpenToPublic());
        }
        configRepository.save(configEntity);

        incrementConfigVersion();
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

    public boolean checkVerificationCode(String verificationCode) throws IllegalStateException {
        String redisVerificationCode = redisService.get(INITIALIZATION_CODE_KEY);
        if (redisVerificationCode == null) throw new IllegalStateException("Initialization code is not set yet.");
        return redisVerificationCode.equals(verificationCode);
    }
}
