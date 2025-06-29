package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user with admin-specific information.
 * This class extends UserDto and includes additional fields for admin-specific information.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminDto extends UserDto {
    private Boolean isBanned;
    private String banReason;
    private LocalDateTime bannedAt;
    private LocalDateTime banExpiresAt;
    private Boolean isPendingIpBan;
    private String ipBanReason;
    private LocalDateTime ipBanExpiresAt;
    private Boolean isAdmin;
}
