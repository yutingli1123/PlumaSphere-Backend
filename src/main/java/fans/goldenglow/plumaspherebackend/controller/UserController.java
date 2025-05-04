package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.UserDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        Set<UserDto> userDtos = users.stream().map(user -> new UserDto(user.getId(), user.getUsername(), user.getNickname(), user.getAvatarUrl(), user.getDob(), user.getCreatedAt(), user.getUpdatedAt(), user.getLastLoginAt())).collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getSelf(JwtAuthenticationToken token) {
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        User userEntity = user.get();
        return ResponseEntity.ok(new UserDto(userEntity.getId(), userEntity.getUsername(), userEntity.getNickname(), userEntity.getAvatarUrl(), userEntity.getDob(), userEntity.getCreatedAt(), userEntity.getUpdatedAt(), userEntity.getLastLoginAt()));
    }
}
