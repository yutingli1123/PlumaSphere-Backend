package fans.goldenglow.plumaspherebackend.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeWebSocketMessageDto implements BaseWebSocketMessageDto {
    private String likeCount;
}
