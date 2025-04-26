package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.TokenResponseDto;
import fans.goldenglow.plumaspherebackend.dto.UserLoginDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.TokenService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class LoginController {
    private final Argon2PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);

    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    public LoginController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

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

        if (passwordEncoder.matches(rawPassword, password)) {
            userEntity.setLastLoginAt(LocalDateTime.now());
            userService.save(userEntity);
            TokenResponseDto responseDto = tokenService.generateTokens(userId, List.of(role.toString().toLowerCase()));
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
