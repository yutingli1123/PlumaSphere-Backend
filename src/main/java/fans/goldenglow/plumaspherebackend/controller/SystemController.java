package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.InitDto;
import fans.goldenglow.plumaspherebackend.dto.StringDto;
import fans.goldenglow.plumaspherebackend.entity.Config;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.ConfigService;
import fans.goldenglow.plumaspherebackend.service.PasswordService;
import fans.goldenglow.plumaspherebackend.service.RedisService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fans.goldenglow.plumaspherebackend.constant.RedisKey.INITIALIZATION_CODE_KEY;

/**
 * Controller for managing system status and initialization.
 * Provides endpoints to get system status, initialize the system, verify codes, and set system configurations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SystemController {
    private final ConfigService configService;
    private final RedisService redisService;
    private final PasswordService passwordService;
    private final UserService userService;

    /**
     * Endpoint to get the current system status.
     * If the user has admin privileges, returns all configurations; otherwise, returns public configurations.
     *
     * @param token the JWT authentication token of the user
     * @return a ResponseEntity containing the list of configurations
     */
    @GetMapping("/status")
    public ResponseEntity<List<Config>> getStatus(JwtAuthenticationToken token) {
        if (token != null) {
            Map<String, Object> attributes = token.getTokenAttributes();
            if (attributes.containsKey("scope") && ((String) attributes.get("scope")).contains("admin")) {
                return ResponseEntity.ok(configService.getAll());
            }
        }
        return ResponseEntity.ok(configService.getAllPublic());
    }

    /**
     * Endpoint to get the current system version.
     * Returns the version from the configuration, defaulting to "1" if not set.
     *
     * @return a ResponseEntity containing the version string
     */
    @GetMapping("/status/version")
    public ResponseEntity<String> getStatusVersion() {
        return ResponseEntity.ok(configService.get(ConfigField.CONFIG_VERSION).orElse("1"));
    }

    /**
     * Endpoint to initialize the system.
     *
     * @param initDto the initialization data containing admin username, password, nickname, blog title, subtitle, and verification code
     * @return a ResponseEntity indicating the result of the initialization
     */
    @PostMapping("/init")
    public ResponseEntity<Void> initSystem(@RequestBody InitDto initDto) {
        try {
            String verificationCode = initDto.getVerificationCode();
            boolean verified = configService.checkVerificationCode(verificationCode);
            if (!verified) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            User user = new User(initDto.adminUsername, passwordService.encodePassword(initDto.adminPassword), initDto.getAdminNickname());
            user.setRole(UserRoles.ADMIN);
            userService.save(user);
            configService.set(ConfigField.BLOG_TITLE, initDto.getBlogTitle());
            configService.set(ConfigField.BLOG_SUBTITLE, initDto.getBlogSubtitle());
            configService.set(ConfigField.INITIALIZED, "true");
            redisService.delete(INITIALIZATION_CODE_KEY);

            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to verify the initialization code.
     *
     * @param dto the DTO containing the verification code
     * @return a ResponseEntity indicating whether the verification was successful
     */
    @PostMapping("/init/verify-code")
    public ResponseEntity<Boolean> verifyCode(@RequestBody StringDto dto) {
        String verifyCode = dto.getValue();
        try {
            return ResponseEntity.ok(configService.checkVerificationCode(verifyCode));
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint to set system configurations.
     *
     * @param config the array of configuration items to be set
     * @return a ResponseEntity indicating the result of the operation
     */
    @PostMapping("/settings")
    public ResponseEntity<Void> setSystemConfig(@RequestBody Config[] config) {
        for (Config configItem : config) {
            Optional<ConfigField> configFieldOpt = ConfigField.tryParse(configItem.getConfigKey());
            if (configFieldOpt.isPresent()) {
                ConfigField configField = configFieldOpt.get();
                configService.set(configField, configItem.getConfigValue());
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok().build();
    }
}
