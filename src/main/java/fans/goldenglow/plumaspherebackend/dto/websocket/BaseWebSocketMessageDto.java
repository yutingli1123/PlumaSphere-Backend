package fans.goldenglow.plumaspherebackend.dto.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface BaseWebSocketMessageDto {
    default public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert WebSocketMessageDto to JSON", e);
        }
    }
}
