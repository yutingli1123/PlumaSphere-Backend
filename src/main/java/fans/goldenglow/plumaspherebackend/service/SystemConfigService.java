package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.SystemConfig;
import fans.goldenglow.plumaspherebackend.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
public class SystemConfigService {
    @Autowired
    private SystemConfigRepository systemConfigRepository;

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

    public String generateSecretKey(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            log.info(encodedKey);

            return encodedKey;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
