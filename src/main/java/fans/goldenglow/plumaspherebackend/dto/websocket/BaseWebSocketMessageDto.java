package fans.goldenglow.plumaspherebackend.dto.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base interface for WebSocket message DTOs.
 * This interface provides a method to convert the implementing class to JSON format.
 */
public interface BaseWebSocketMessageDto {
    default String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert WebSocketMessageDto to JSON", e);
        }
    }
}
