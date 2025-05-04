package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.ZonedDateTime;

@Data
public class TokenResponseDto {
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


