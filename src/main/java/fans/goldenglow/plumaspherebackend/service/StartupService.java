package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

import static fans.goldenglow.plumaspherebackend.constant.RedisKey.INITIALIZATION_CODE_KEY;

/**
 * Service for handling application startup tasks.
 * Initializes the application by generating and storing a verification code in Redis.
 * The code is logged for reference.
 */
@Component
@Slf4j
public class StartupService {
    private static final String YELLOW_ANSI = "\u001B[33m";
    private static final String RESET_ANSI = "\u001B[0m";
    private final ConfigService configService;
    private final RedisService redisService;

    /**
     * Constructs a StartupService with the provided ConfigService and RedisService.
     *
     * @param configService the configuration service to check initialization status
     * @param redisService  the Redis service to manage the verification code
     */
    public StartupService(ConfigService configService, RedisService redisService) {
        this.configService = configService;
        this.redisService = redisService;
    }

    /**
     * Initializes the application by checking if it has been initialized before.
     * If not, generates a verification code, stores it in Redis, and logs it.
     * This method is called after the bean is constructed.
     */
    @PostConstruct
    public void init() {
        if (configService.get(ConfigField.INITIALIZED).isPresent()) return;

        String verificationCode = redisService.get(INITIALIZATION_CODE_KEY);

        if (verificationCode == null) {
            verificationCode = generateVerificationCode();
            redisService.set(INITIALIZATION_CODE_KEY, verificationCode);
        }

        log.info(YELLOW_ANSI + "Initialization Code: {}" + RESET_ANSI, verificationCode);
    }

    /**
     * Generates a random verification code consisting of 6 digits.
     * The code is generated using a secure random number generator.
     *
     * @return a string representation of the verification code
     */
    private String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(100000, 999999));
    }
}
