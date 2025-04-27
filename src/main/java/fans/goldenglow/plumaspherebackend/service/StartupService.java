package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

import static fans.goldenglow.plumaspherebackend.constant.RedisKey.INITIALIZATION_CODE_KEY;

@Component
@Slf4j
public class StartupService {
    private static final String YELLOW_ANSI = "\u001B[33m";
    private static final String RESET_ANSI = "\u001B[0m";
    private final ConfigService configService;
    private final RedisService redisService;

    public StartupService(ConfigService configService, RedisService redisService) {
        this.configService = configService;
        this.redisService = redisService;
    }

    @PostConstruct
    public void init() {
        String initialized = configService.get(ConfigField.INITIALIZED).orElse("");
        if (configService.get(ConfigField.INITIALIZED).isPresent()) return;

        String verificationCode = redisService.get(INITIALIZATION_CODE_KEY);

        if (verificationCode == null) {
            verificationCode = generateVerificationCode();
            redisService.set(INITIALIZATION_CODE_KEY, verificationCode);
        }

        log.info(YELLOW_ANSI + "Initialization Code: {}" + RESET_ANSI, verificationCode);
    }

    private String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(100000, 999999));
    }
}
