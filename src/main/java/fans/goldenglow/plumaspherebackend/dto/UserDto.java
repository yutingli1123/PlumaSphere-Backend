package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * DTO for user information.
 * This class is used to represent a user with their ID, username, nickname, bio, avatar URL,
 * avatar color, initials, date of birth, and timestamps for creation, last update, and last login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String nickname;
    private String bio;
    private String avatarUrl;
    private String avatarColor;
    private String initials;
    private LocalDate dob;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime lastLoginAt;
}
