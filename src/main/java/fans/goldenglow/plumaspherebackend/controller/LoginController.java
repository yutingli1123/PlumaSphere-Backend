package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.annotation.CheckIpBan;
import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.StringDto;
import fans.goldenglow.plumaspherebackend.dto.TokenPairResponseDto;
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

/**
 * Controller for handling user login and token management.
 * Provides endpoints for user login, token refresh, and identity retrieval.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordService passwordService;

    /**
     * Endpoint for user login.
     * Validates the user's credentials and returns a token response if successful.
     *
     * @param loginData the user's login data containing username and password
     * @return a ResponseEntity containing the token response or an unauthorized status
     */
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponseDto> login(@RequestBody UserLoginDto loginData) {
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
            TokenPairResponseDto responseDto = tokenService.generateTokens(userId, List.of(role.toString().toLowerCase()));
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Endpoint to refresh the user's token.
     * Validates the provided refresh token and returns a new token response if valid.
     *
     * @param dto the DTO containing the refresh token
     * @return a ResponseEntity containing the new token response or an unauthorized status
     */
    @CheckIpBan
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenPairResponseDto> refreshToken(@RequestBody StringDto dto) {
        String refreshToken = dto.getValue();
        TokenPairResponseDto responseDto = tokenService.refreshToken(refreshToken);
        if (responseDto == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Endpoint to retrieve the identity of the user.
     * Generates a new user identity if the user does not exist and returns a token response.
     *
     * @return a ResponseEntity containing the token response
     */
    @CheckIpBan
    @GetMapping("/get-identity")
    @RateLimiter(name = "get-identity", fallbackMethod = "getIdentityFallback")
    public ResponseEntity<TokenPairResponseDto> getIdentity() {
        String username = RandomUtil.generateRandomUsername();
        while (userService.existByUsername(username)) username = RandomUtil.generateRandomUsername();
        User user = new User(username, passwordService.generateRandomPassword());
        userService.save(user);
        TokenPairResponseDto responseDto = tokenService.generateTokens(user.getId(), List.of(user.getRole().toString().toLowerCase()));
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Fallback method for the getIdentity endpoint when the rate limit is exceeded.
     * Returns a 429 Too Many Requests status with a message indicating high demand.
     *
     * @param ignoredException the exception that triggered the fallback
     * @return a ResponseEntity with a status of 429 and a message
     */
    public ResponseEntity<String> getIdentityFallback(Exception ignoredException) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("API rate limit reached. The service is currently experiencing high demand. Please try again later.");
    }

}
