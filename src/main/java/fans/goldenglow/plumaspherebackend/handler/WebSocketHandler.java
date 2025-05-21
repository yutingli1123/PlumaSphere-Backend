package fans.goldenglow.plumaspherebackend.handler;

import fans.goldenglow.plumaspherebackend.dto.websocket.WebSocketMessageDto;
import io.micrometer.common.lang.NonNullApi;
import lombok.AllArgsConstructor;
import lombok.Data;
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
    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> postWebSocketSessionMap = new ConcurrentHashMap<>();
    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> commentWebSocketSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Optional<ConnectionInfo> connectionInfoOption = getConnectionTypeFromSession(session);
        if (connectionInfoOption.isPresent()) {
            ConnectionInfo connectionInfo = connectionInfoOption.get();
            ConnectionType connectionType = connectionInfo.getConnectionType();
            switch (connectionType) {
                case POST: {
                    Long postId = connectionInfo.getTargetId();
                    postWebSocketSessionMap.computeIfAbsent(postId, key -> new CopyOnWriteArraySet<>()).add(session);
                    break;
                }
                case COMMENT: {
                    Long commentId = connectionInfo.getTargetId();
                    commentWebSocketSessionMap.computeIfAbsent(commentId, key -> new CopyOnWriteArraySet<>()).add(session);
                    break;
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Optional<ConnectionInfo> connectionInfoOption = getConnectionTypeFromSession(session);
        if (connectionInfoOption.isPresent()) {
            ConnectionInfo connectionInfo = connectionInfoOption.get();
            ConnectionType connectionType = connectionInfo.getConnectionType();
            Long targetId = connectionInfo.getTargetId();
            switch (connectionType) {
                case POST: {
                    removeSessionFromPost(targetId, session);
                    break;
                }
                case COMMENT: {
                    removeSessionFromComment(targetId, session);
                    break;
                }
            }
        }
    }

    private void removeSessionFromPost(Long postId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> webSocketSessions = postWebSocketSessionMap.get(postId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()) {
                postWebSocketSessionMap.remove(postId);
            }
        }
    }

    private void removeSessionFromComment(Long commentId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> webSocketSessions = commentWebSocketSessionMap.get(commentId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()) {
                commentWebSocketSessionMap.remove(commentId);
            }
        }
    }

    public void sendMessageToPost(Long postId, WebSocketMessageDto message) {
        SendMessageToTarget(postId, message, postWebSocketSessionMap);
    }

    public void sendMessageToComment(Long commentId, WebSocketMessageDto message) {
        SendMessageToTarget(commentId, message, commentWebSocketSessionMap);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        session.close();
    }

    private void SendMessageToTarget(Long targetId, WebSocketMessageDto message, Map<Long, CopyOnWriteArraySet<WebSocketSession>> commentWebSocketSessionMap) {
        CopyOnWriteArraySet<WebSocketSession> webSocketSessions = commentWebSocketSessionMap.get(targetId);
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

    private Optional<ConnectionInfo> getConnectionTypeFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return Optional.empty();
        String query = uri.getQuery();
        if (query == null) return Optional.empty();

        Optional<ConnectionInfo> connectionInfo = Optional.empty();
        if (query.contains("postId=")) {
            connectionInfo = Optional.of(new ConnectionInfo(ConnectionType.POST, Long.parseLong(query.split("=")[1])));
        } else if (query.contains("commentId=")) {
            connectionInfo = Optional.of(new ConnectionInfo(ConnectionType.COMMENT, Long.parseLong(query.split("=")[1])));
        }
        return connectionInfo;
    }

    enum ConnectionType {
        POST,
        COMMENT
    }

    @Data
    @AllArgsConstructor
    static class ConnectionInfo {
        private ConnectionType connectionType;
        private Long targetId;
    }
}
