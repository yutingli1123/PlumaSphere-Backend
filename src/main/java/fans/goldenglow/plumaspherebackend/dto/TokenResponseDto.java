package fans.goldenglow.plumaspherebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class TokenResponseDto {
    @JsonProperty("access_token")
    @NonNull
    private TokenDetails accessToken;
    @JsonProperty("refresh_token")
    @NonNull
    private TokenDetails refreshToken;

    @AllArgsConstructor
    @Data
    public static class TokenDetails {
        private String token;
        private LocalDateTime expiresAt;
    }
}


