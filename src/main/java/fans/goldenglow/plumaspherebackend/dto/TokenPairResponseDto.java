package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.ZonedDateTime;

/**
 * DTO for a pair of tokens (access and refresh).
 */
@Data
public class TokenPairResponseDto {
    @NonNull
    private TokenDetails accessToken;
    @NonNull
    private TokenDetails refreshToken;

    @AllArgsConstructor
    @Data
    public static class TokenDetails {
        private String token;
        private ZonedDateTime expiresAt;
    }
}


