package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for banning a user.
 * This class is used to send requests to ban a user with a reason and expiration time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanRequestDto {
    private Long userId;
    private String reason;
    private ZonedDateTime expiresAt;
}
