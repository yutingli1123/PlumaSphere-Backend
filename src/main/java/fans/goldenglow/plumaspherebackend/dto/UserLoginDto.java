package fans.goldenglow.plumaspherebackend.dto;

import lombok.Data;
import lombok.NonNull;

/**
 * DTO for user login.
 * This class is used to send login requests with username and password.
 */
@Data
public class UserLoginDto {
    @NonNull
    private String username;
    @NonNull
    private String password;
}
