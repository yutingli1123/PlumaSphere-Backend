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

/**
 * Service for managing configuration settings in the application.
 * Provides methods to get, set, and manage configuration fields.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigService {
    private final ConfigRepository configRepository;
    private final RedisService redisService;

    /**
     * A set of immutable configuration fields that cannot be modified after initial setup.
     * These fields are critical for the system's integrity and should not be changed.
     */
    private static final Set<ConfigField> IMMUTABLE_CONFIG_FIELDS = Set.of(
            ConfigField.CONFIG_VERSION
    );

    /**
     * Retrieves the value of a specific configuration field.
     *
     * @param configField the configuration field to retrieve
     * @return an Optional containing the value of the configuration field, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<String> get(ConfigField configField) {
        return configRepository.findByConfigKey(configField.name().toLowerCase()).map(Config::getConfigValue);
    }

    /**
     * Retrieves all configuration settings.
     *
     * @return a list of all configuration settings
     */
    @Transactional(readOnly = true)
    public List<Config> getAll() {
        return configRepository.findAll();
    }

    /**
     * Retrieves all public configuration settings that are open to the public.
     *
     * @return a list of public configuration settings
     */
    @Transactional(readOnly = true)
    public List<Config> getAllPublic() {
        List<Config> configs = configRepository.findAll();
        return configs.stream().filter(Config::getIsOpenToPublic).toList();
    }

    /**
     * Sets the value of a specific configuration field.
     * If the field is immutable or if the system is already initialized, it will not allow changes.
     *
     * @param configField the configuration field to set
     * @param value       the value to set for the configuration field
     */
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

    /**
     * Increments the configuration version.
     */
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

    /**
     * Checks if the provided verification code matches the one stored in Redis.
     *
     * @param verificationCode the verification code to check
     * @return true if the verification code matches, false otherwise
     * @throws IllegalStateException if the initialization code is not set yet
     */
    public boolean checkVerificationCode(String verificationCode) throws IllegalStateException {
        String redisVerificationCode = redisService.get(INITIALIZATION_CODE_KEY);
        if (redisVerificationCode == null) throw new IllegalStateException("Initialization code is not set yet.");
        return redisVerificationCode.equals(verificationCode);
    }
}
