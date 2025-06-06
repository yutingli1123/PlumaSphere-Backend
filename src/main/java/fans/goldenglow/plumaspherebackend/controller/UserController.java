package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.UserDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import fans.goldenglow.plumaspherebackend.mapper.UserMapper;
import fans.goldenglow.plumaspherebackend.service.FileService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(userMapper.toDto(users));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getSelf(JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return getUserDtoResponseEntityFromUserId(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return getUserDtoResponseEntityFromUserId(userId);
    }

    @PutMapping
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserDto userDto, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> userOptional = userService.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setNickname(userDto.getNickname());
            user.setBio(userDto.getBio());
            user.setDob(userDto.getDob());
            userService.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/avatar")
    public ResponseEntity<Void> updateUserAvatar(@RequestParam("file") MultipartFile file, JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        Optional<User> userOptional = userService.findById(userId);

        if (userOptional.isPresent()) {
            try {
                String newAvatarUrl = fileService.saveFile(file);
                User user = userOptional.get();
                user.setAvatarUrl(newAvatarUrl);
                userService.save(user);
                return ResponseEntity.ok().build();
            } catch (FileSaveException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<UserDto> getUserDtoResponseEntityFromUserId(Long userId) {
        Optional<User> user = userService.findById(userId);
        return user.map(value -> ResponseEntity.ok(userMapper.toDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
