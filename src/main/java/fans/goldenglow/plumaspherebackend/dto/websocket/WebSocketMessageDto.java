package fans.goldenglow.plumaspherebackend.dto.websocket;

import fans.goldenglow.plumaspherebackend.constant.WebSocketMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketMessageDto implements BaseWebSocketMessageDto {
    private WebSocketMessageType type;
    private String data;

    public WebSocketMessageDto(WebSocketMessageType type) {
        this.type = type;
    }
}
