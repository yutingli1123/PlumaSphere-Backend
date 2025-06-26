package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for banning an IP address.
 * This class is used to send requests to ban an IP address with a reason and expiration time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanIPRequestDto {
    private String ipAddress;
    private String reason;
    private ZonedDateTime expiresAt;
}
