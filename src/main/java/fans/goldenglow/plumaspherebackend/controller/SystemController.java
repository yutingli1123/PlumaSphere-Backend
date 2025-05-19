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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static fans.goldenglow.plumaspherebackend.constant.RedisKey.INITIALIZATION_CODE_KEY;

@RestController
@RequestMapping("/api/v1")
public class SystemController {
    private final ConfigService configService;
    private final RedisService redisService;
    private final PasswordService passwordService;
    private final UserService userService;

    @Autowired
    public SystemController(ConfigService configService, RedisService redisService, PasswordService passwordService, UserService userService) {
        this.configService = configService;
        this.redisService = redisService;
        this.passwordService = passwordService;
        this.userService = userService;
    }

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

    @PostMapping("/init")
    public ResponseEntity<Void> initSystem(@RequestBody InitDto initDto) {
        try {
            String verificationCode = initDto.getVerificationCode();
            boolean verified = verify(verificationCode);
            if (!verified) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            User user = new User(initDto.adminUsername, passwordService.encodePassword(initDto.adminPassword), initDto.getAdminNickname());
            user.setRole(UserRoles.ADMIN);
            userService.save(user);
            configService.set(ConfigField.BLOG_TITLE, initDto.getBlogTitle(), true);
            configService.set(ConfigField.BLOG_SUBTITLE, initDto.getBlogSubtitle(), true);

            configService.set(ConfigField.INITIALIZED, "true", true);
            redisService.delete(INITIALIZATION_CODE_KEY);

            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/init/verify-code")
    public ResponseEntity<Boolean> verifyCode(@RequestBody StringDto dto) {
        String verifyCode = dto.getValue();
        try {
            return ResponseEntity.ok(verify(verifyCode));
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean verify(String verificationCode) throws IllegalStateException {
        String redisVerificationCode = redisService.get(INITIALIZATION_CODE_KEY);
        if (redisVerificationCode == null) throw new IllegalStateException("Initialization code is not set yet.");
        return redisVerificationCode.equals(verificationCode);
    }
}
