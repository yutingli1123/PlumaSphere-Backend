package fans.goldenglow.plumaspherebackend.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import fans.goldenglow.plumaspherebackend.dto.UserLoginDto;
import fans.goldenglow.plumaspherebackend.entity.SystemConfig;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.SystemConfigService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class LoginController {
    private final Argon2PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);

    @Autowired
    private UserService userService;
    @Autowired
    private SystemConfigService systemConfigService;

    @PostMapping("/init")
    public ResponseEntity<Boolean> initUser(@RequestBody UserLoginDto userLoginDto) {
        User user = new User(userLoginDto.getUsername(), passwordEncoder.encode(userLoginDto.getPassword()));
        if (userService.save(user)) {
            return ResponseEntity.ok(systemConfigService.set(new SystemConfig("init_complete", "true")));
        }
        return ResponseEntity.internalServerError().build();
    }

    @PostMapping("/login")
    public ResponseEntity<HashMap<String, String>> login(@RequestBody UserLoginDto loginData) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + 15 * 60 * 1000);
        Date refresh_expire = new Date(now.getTime() + 20 * 60 * 1000);
        String username = loginData.getUsername();
        String rawPassword = loginData.getPassword();

        User user = userService.findByUsername(username).orElse(null);
        if (user != null) {
            String password = user.getPassword();
            String secretKey;
            secretKey = systemConfigService.get("secret_key").orElse(systemConfigService.generateSecretKey());

            if (passwordEncoder.matches(rawPassword, password)) {
                HashMap<String, String> responseData = new HashMap<>();
                responseData.put("token", JWT.create().withIssuer(username).withIssuedAt(now).withExpiresAt(expire).sign(Algorithm.HMAC256(secretKey)));
                responseData.put("refresh_token", JWT.create().withIssuer(username).withIssuedAt(now).withExpiresAt(refresh_expire).sign(Algorithm.HMAC256(secretKey)));
                return ResponseEntity.ok(responseData);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
