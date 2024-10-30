package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.UserLoginDto;
import fans.goldenglow.plumaspherebackend.entity.SystemConfig;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.SystemConfigService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import fans.goldenglow.plumaspherebackend.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/init")
    public ResponseEntity<Boolean> initUser(@RequestBody UserLoginDto userLoginDto) {
        User user = new User(userLoginDto.getUsername(), passwordEncoder.encode(userLoginDto.getPassword()));
        user.setRole(UserRoles.ROLE_ADMIN);
        if (userService.save(user)) {
            return ResponseEntity.ok(systemConfigService.set(new SystemConfig("initialled", "true")));
        }
        return ResponseEntity.internalServerError().build();
    }

    @PostMapping("/login")
    public ResponseEntity<HashMap<String, String>> login(@RequestBody UserLoginDto loginData) {
        String username = loginData.getUsername();
        String rawPassword = loginData.getPassword();

        User user = userService.findByUsername(username).orElse(null);
        if (user != null) {
            String password = user.getPassword();

            if (passwordEncoder.matches(rawPassword, password)) {
                HashMap<String, String> responseData = jwtUtil.generateToken(username);
                return ResponseEntity.ok(responseData);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<HashMap<String, String>> register(@RequestBody UserLoginDto loginData) {

        String username = loginData.getUsername();
        String rawPassword = loginData.getPassword();

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            User newUser = new User(username, passwordEncoder.encode(rawPassword));
            newUser.setRole(UserRoles.ROLE_REGULAR);
            if (userService.save(newUser)) {
                HashMap<String, String> responseData = jwtUtil.generateToken(username);
                return ResponseEntity.ok(responseData);
            } else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/status")
    public ResponseEntity<HashMap<String, String>> getStatus() {
        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("isInit", systemConfigService.get("initialled").orElse("false"));
        return ResponseEntity.ok(responseData);
    }
}
