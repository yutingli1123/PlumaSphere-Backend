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

    /**
     * Endpoint to get all users with pagination.
     * Returns a list of UserAdminDto objects representing the users.
     *
     * @param page the page number to retrieve
     * @return ResponseEntity containing the list of UserAdminDto objects
     */
    @GetMapping
    public ResponseEntity<List<UserAdminDto>> getAllUsers(@RequestParam int page) {
        return ResponseEntity.ok(userMapper.toAdminDto(userService.findAll(PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id")))));
    }

    /**
     * Endpoint to get the total number of users.
     *
     * @return ResponseEntity containing the total number of users
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        long totalUsers = userService.countAll();
        return ResponseEntity.ok(totalUsers);
    }

    /**
     * Endpoint to get the total number of user pages based on the PAGE_SIZE.
     *
     * @return ResponseEntity containing the total number of user pages
     */
    @GetMapping("/count-page")
    public ResponseEntity<Long> getUserPageCount() {
        long totalUsers = userService.countAll();
        long pageCount = (long) Math.ceil((double) totalUsers / PAGE_SIZE); // Calculate total pages
        return ResponseEntity.ok(pageCount);
    }

    /**
     * Endpoint to get the current user's information.
     *
     * @param token the JWT authentication token of the user
     * @return ResponseEntity containing the UserDto of the current user
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getSelf(JwtAuthenticationToken token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(token.getToken().getSubject());
        return getUserDtoResponseEntityFromUserId(userId);
    }

    /**
     * Endpoint to get a user by their ID.
     * If the user is an admin, returns the UserAdminDto; otherwise, returns the UserDto.
     *
     * @param userId the ID of the user to retrieve
     * @param token  the JWT authentication token of the user
     * @return ResponseEntity containing the UserDto or UserAdminDto of the requested user
     */
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

    /**
     * Endpoint to update the current user's information.
     *
     * @param userDto the UserDto containing the updated user information
     * @param token   the JWT authentication token of the user
     * @return ResponseEntity indicating the result of the update operation
     */
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

    /**
     * Endpoint to update the current user's avatar.
     *
     * @param file  the new avatar file to upload
     * @param token the JWT authentication token of the user
     * @return ResponseEntity indicating the result of the update operation
     */
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

    /**
     * Endpoint to delete a user by their ID.
     *
     * @param userId the ID of the user to delete
     * @return ResponseEntity indicating the result of the deletion operation
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteById(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to get the user information by their ID.
     * If the user is an admin, returns the UserAdminDto; otherwise, returns the UserDto.
     *
     * @param userId the ID of the user to retrieve
     * @return ResponseEntity containing the UserDto or UserAdminDto of the requested user
     */
    private ResponseEntity<UserDto> getUserDtoResponseEntityFromUserId(Long userId) {
        Optional<User> user = userService.findById(userId);
        return user.map(value -> ResponseEntity.ok(userMapper.toDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to get the user information by their ID.
     * If the user is an admin, returns the UserAdminDto; otherwise, returns the UserDto.
     *
     * @param userId  the ID of the user to retrieve
     * @param isAdmin indicates if the request is made by an admin
     * @return ResponseEntity containing the UserDto or UserAdminDto of the requested user
     */
    private ResponseEntity<?> getUserDtoResponseEntityFromUserId(Long userId, boolean isAdmin) {
        if (isAdmin) {
            Optional<User> user = userService.findById(userId);
            return user.map(value -> ResponseEntity.ok(userMapper.toAdminDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
        }
        return getUserDtoResponseEntityFromUserId(userId);
    }
}
