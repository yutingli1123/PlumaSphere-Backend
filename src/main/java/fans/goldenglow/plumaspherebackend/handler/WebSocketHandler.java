package fans.goldenglow.plumaspherebackend.handler;

import fans.goldenglow.plumaspherebackend.dto.websocket.WebSocketMessageDto;
import io.micrometer.common.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@NonNullApi
public class WebSocketHandler extends TextWebSocketHandler {
    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> webSocketSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Optional<Long> postIdOption = getPostIdFromSession(session);
        postIdOption.ifPresent(postId -> webSocketSessionMap
                .computeIfAbsent(postId, key -> new CopyOnWriteArraySet<>())
                .add(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Optional<Long> postIdOption = getPostIdFromSession(session);
        if (postIdOption.isPresent()) {
            Long postId = postIdOption.get();
            CopyOnWriteArraySet<WebSocketSession> webSocketSessions = webSocketSessionMap.get(postId);
            if (webSocketSessions != null) {
                webSocketSessions.remove(session);
                if (webSocketSessions.isEmpty()) {
                    webSocketSessionMap.remove(postId);
                }
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        session.close();
    }

    public void sendMessageToPost(Long postId, WebSocketMessageDto message) {
        CopyOnWriteArraySet<WebSocketSession> webSocketSessions = webSocketSessionMap.get(postId);
        if (webSocketSessions != null) {
            for (WebSocketSession session : webSocketSessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message.toJson()));
                    } catch (IOException e) {
                        log.warn("WebSocket: ", e);
                    }
                }
            }
        }
    }

    private Optional<Long> getPostIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return Optional.empty();
        String query = uri.getQuery();
        if (query != null && query.contains("postId=")) {
            return Optional.of(Long.valueOf(query.split("=")[1]));
        }
        return Optional.empty();
    }
}
