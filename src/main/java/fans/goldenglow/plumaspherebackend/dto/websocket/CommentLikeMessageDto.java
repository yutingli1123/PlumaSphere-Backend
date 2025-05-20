package fans.goldenglow.plumaspherebackend.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentLikeMessageDto implements BaseWebSocketMessageDto {
    private Long commentId;
}
