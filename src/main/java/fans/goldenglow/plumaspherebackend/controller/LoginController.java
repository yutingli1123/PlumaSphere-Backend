package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.annotation.CheckIpBan;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.StringDto;
import fans.goldenglow.plumaspherebackend.dto.TokenResponseDto;
import fans.goldenglow.plumaspherebackend.dto.UserLoginDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.PasswordService;
import fans.goldenglow.plumaspherebackend.service.TokenService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import fans.goldenglow.plumaspherebackend.util.RandomUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordService passwordService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody UserLoginDto loginData) {
        String username = loginData.getUsername();
        String rawPassword = loginData.getPassword();

        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User userEntity = user.get();
        String password = userEntity.getPassword();
        Long userId = userEntity.getId();
        UserRoles role = userEntity.getRole();

        if (passwordService.verifyPassword(rawPassword, password)) {
            userEntity.setLastLoginAt(LocalDateTime.now());
            userService.save(userEntity);
            TokenResponseDto responseDto = tokenService.generateTokens(userId, List.of(role.toString().toLowerCase()));
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @CheckIpBan
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponseDto> refreshToken(@RequestBody StringDto dto) {
        String refreshToken = dto.getValue();
        TokenResponseDto responseDto = tokenService.refreshToken(refreshToken);
        if (responseDto == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(responseDto);
    }

    @CheckIpBan
    @GetMapping("/get-identity")
    @RateLimiter(name = "get-identity", fallbackMethod = "getIdentityFallback")
    public ResponseEntity<TokenResponseDto> getIdentity() {
        String username = RandomUtil.generateRandomUsername();
        while (userService.existByUsername(username)) username = RandomUtil.generateRandomUsername();
        User user = new User(username, passwordService.generateRandomPassword());
        userService.save(user);
        TokenResponseDto responseDto = tokenService.generateTokens(user.getId(), List.of(user.getRole().toString().toLowerCase()));
        return ResponseEntity.ok(responseDto);
    }

    public ResponseEntity<String> getIdentityFallback(Exception exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("API rate limit reached. The service is currently experiencing high demand. Please try again later.");
    }

}
