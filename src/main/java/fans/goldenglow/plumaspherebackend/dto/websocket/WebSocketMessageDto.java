package fans.goldenglow.plumaspherebackend.dto.websocket;

import fans.goldenglow.plumaspherebackend.constant.WebSocketMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for WebSocket messages.
 * This class is used to send messages over WebSocket connections.
 */
@Data
@AllArgsConstructor
public class WebSocketMessageDto implements BaseWebSocketMessageDto {
    private WebSocketMessageType type;
    private Object data;

    public WebSocketMessageDto(WebSocketMessageType type) {
        this.type = type;
    }
}
