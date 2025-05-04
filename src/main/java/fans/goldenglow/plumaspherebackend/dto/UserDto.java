package fans.goldenglow.plumaspherebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String nickname;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private LocalDate dob;
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;
    @JsonProperty("last_login_at")
    private ZonedDateTime lastLoginAt;
}
