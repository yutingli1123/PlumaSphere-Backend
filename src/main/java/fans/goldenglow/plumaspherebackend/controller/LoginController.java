package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.TokenResponseDto;
import fans.goldenglow.plumaspherebackend.dto.UserLoginDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.PasswordService;
import fans.goldenglow.plumaspherebackend.service.TokenService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class LoginController {
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordService passwordService;

    @Autowired
    public LoginController(UserService userService, TokenService tokenService, PasswordService passwordService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
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

        if (passwordService.verifyPassword(rawPassword, password)) {
            userEntity.setLastLoginAt(LocalDateTime.now());
            userService.save(userEntity);
            TokenResponseDto responseDto = tokenService.generateTokens(userId, List.of(role.toString().toLowerCase()));
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponseDto> refreshToken(@RequestBody String refreshToken) {
        TokenResponseDto responseDto = tokenService.refreshToken(refreshToken);
        if (responseDto == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(responseDto);
    }

}
