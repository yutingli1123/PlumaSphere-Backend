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

/**
 * WebSocketHandler for managing WebSocket connections for posts and comments.
 * This handler maintains separate session maps for posts and comments,
 * allowing messages to be sent to specific sessions based on the target ID.
 */
@Slf4j
@Component
@NonNullApi
public class WebSocketHandler extends TextWebSocketHandler {
    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> postWebSocketSessionMap = new ConcurrentHashMap<>();
    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> commentWebSocketSessionMap = new ConcurrentHashMap<>();

    /**
     * Handles the establishment of a new WebSocket connection.
     * It determines the type of connection (post or comment) and adds the session to the appropriate map.
     *
     * @param session the WebSocket session that has been established
     */
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

    /**
     * Handles the closure of a WebSocket connection.
     * It removes the session from the appropriate map based on the connection type.
     *
     * @param session the WebSocket session that has been closed
     * @param status  the status of the closure
     */
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

    /**
     * Removes the session from the post WebSocket session map.
     *
     * @param postId  the ID of the post
     * @param session the WebSocket session to be removed
     */
    private void removeSessionFromPost(Long postId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> webSocketSessions = postWebSocketSessionMap.get(postId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()) {
                postWebSocketSessionMap.remove(postId);
            }
        }
    }

    /**
     * Removes the session from the comment WebSocket session map.
     *
     * @param commentId the ID of the comment
     * @param session the WebSocket session to be removed
     */
    private void removeSessionFromComment(Long commentId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> webSocketSessions = commentWebSocketSessionMap.get(commentId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()) {
                commentWebSocketSessionMap.remove(commentId);
            }
        }
    }

    /**
     * Sends a message to all WebSocket sessions associated with a specific post ID.
     *
     * @param postId the ID of the post to which the message should be sent
     * @param message the message to be sent
     */
    public void sendMessageToPost(Long postId, WebSocketMessageDto message) {
        SendMessageToTarget(postId, message, postWebSocketSessionMap);
    }

    /**
     * Sends a message to all WebSocket sessions associated with a specific comment ID.
     *
     * @param commentId the ID of the comment to which the message should be sent
     * @param message the message to be sent
     */
    public void sendMessageToComment(Long commentId, WebSocketMessageDto message) {
        SendMessageToTarget(commentId, message, commentWebSocketSessionMap);
    }

    /**
     * Handles incoming WebSocket messages.
     * This implementation simply closes the session upon receiving a message.
     *
     * @param session the WebSocket session that received the message
     * @param message the WebSocket message received
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        session.close();
    }

    /**
     * Sends a message to all WebSocket sessions associated with a specific target ID.
     *
     * @param targetId the ID of the target (post or comment)
     * @param message the message to be sent
     * @param commentWebSocketSessionMap the map containing comment WebSocket sessions
     */
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

    /**
     * Extracts the connection type and target ID from the WebSocket session URI.
     *
     * @param session the WebSocket session from which to extract the connection type
     * @return an Optional containing the ConnectionInfo if the connection type is recognized, otherwise empty
     */
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

    /**
     * Enum representing the type of WebSocket connection.
     * It can either be a connection to a post or a comment.
     */
    private enum ConnectionType {
        POST,
        COMMENT
    }

    /**
     * Class representing the connection information extracted from the WebSocket session.
     * It contains the type of connection and the target ID (post or comment).
     */
    @Data
    @AllArgsConstructor
    private static class ConnectionInfo {
        private ConnectionType connectionType;
        private Long targetId;
    }
}
