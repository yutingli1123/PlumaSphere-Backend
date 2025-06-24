package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.dto.UserAdminDto;
import fans.goldenglow.plumaspherebackend.dto.UserDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import fans.goldenglow.plumaspherebackend.mapper.UserMapper;
import fans.goldenglow.plumaspherebackend.service.FileService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private final int PAGE_SIZE = 10;

    @GetMapping
    public ResponseEntity<List<UserAdminDto>> getAllUsers(@RequestParam int page) {
        return ResponseEntity.ok(userMapper.toAdminDto(userService.findAll(PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id")))));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        long totalUsers = userService.countAll();
        return ResponseEntity.ok(totalUsers);
    }

    @GetMapping("/count-page")
    public ResponseEntity<Long> getUserPageCount() {
        long totalUsers = userService.countAll();
        long pageCount = (long) Math.ceil((double) totalUsers / PAGE_SIZE); // Calculate total pages
        return ResponseEntity.ok(pageCount);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getSelf(JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return getUserDtoResponseEntityFromUserId(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable("userId") Long userId, JwtAuthenticationToken token) {
        if (token != null) {
            Long tokenUserId = Long.parseLong(token.getToken().getSubject());
            Optional<User> userOptional = userService.findById(tokenUserId);
            if (userOptional.isPresent()) {
                User userEntity = userOptional.get();
                if (userEntity.getRole().equals(UserRoles.ADMIN)) {
                    return getUserDtoResponseEntityFromUserId(userId, true);
                }
            }
        }

        return getUserDtoResponseEntityFromUserId(userId);
    }

    @PutMapping
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserDto userDto, JwtAuthenticationToken token) {
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

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteById(userId);
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<UserDto> getUserDtoResponseEntityFromUserId(Long userId) {
        Optional<User> user = userService.findById(userId);
        return user.map(value -> ResponseEntity.ok(userMapper.toDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<?> getUserDtoResponseEntityFromUserId(Long userId, boolean isAdmin) {
        if (isAdmin) {
            Optional<User> user = userService.findById(userId);
            return user.map(value -> ResponseEntity.ok(userMapper.toAdminDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
        }
        return getUserDtoResponseEntityFromUserId(userId);
    }
}
