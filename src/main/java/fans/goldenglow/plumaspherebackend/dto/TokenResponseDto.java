package fans.goldenglow.plumaspherebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class TokenResponseDto {
    @JsonProperty("access_token")
    @NonNull
    private String accessToken;
    @JsonProperty("refresh_token")
    @NonNull
    private String refreshToken;
}
