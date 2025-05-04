package fans.goldenglow.plumaspherebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeDto {
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("user_nickname")
    private String userNickname;
}
