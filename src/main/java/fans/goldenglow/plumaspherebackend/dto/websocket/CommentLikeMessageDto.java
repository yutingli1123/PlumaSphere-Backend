package fans.goldenglow.plumaspherebackend.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for WebSocket messages related to comment likes.
 * This class is used to send messages when a comment is liked.
 */
@Data
@AllArgsConstructor
public class CommentLikeMessageDto implements BaseWebSocketMessageDto {
    private Long commentId;
}
