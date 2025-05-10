package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.UserDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Set<UserDto>> getAllUsers() {
        List<User> users = userService.findAll();

        Set<UserDto> userDtos = users.stream().map(user -> new UserDto(user.getId(), user.getUsername(),
                        user.getNickname(), user.getBio(), user.getAvatarUrl(), user.getDob(),
                        user.getCreatedAt().atZone(ZoneId.systemDefault()),
                        user.getUpdatedAt().atZone(ZoneId.systemDefault()),
                        user.getLastLoginAt().atZone(ZoneId.systemDefault())))
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getSelf(JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return getUserDtoResponseEntity(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return getUserDtoResponseEntity(userId);
    }

    private ResponseEntity<UserDto> getUserDtoResponseEntity(Long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        User userEntity = user.get();
        ZonedDateTime zonedCreatedAt = Optional.ofNullable(userEntity.getCreatedAt())
                .map(time -> time.atZone(ZoneId.systemDefault()))
                .orElse(null);
        ZonedDateTime zonedUpdatedAt = Optional.ofNullable(userEntity.getUpdatedAt())
                .map(time -> time.atZone(ZoneId.systemDefault()))
                .orElse(null);
        ZonedDateTime zonedLastLoginAt = Optional.ofNullable(userEntity.getLastLoginAt())
                .map(time -> time.atZone(ZoneId.systemDefault()))
                .orElse(null);
        return ResponseEntity.ok(new UserDto(userEntity.getId(), userEntity.getUsername(), userEntity.getNickname(),
                userEntity.getBio(), userEntity.getAvatarUrl(), userEntity.getDob(),
                zonedCreatedAt,
                zonedUpdatedAt,
                zonedLastLoginAt));
    }
}
