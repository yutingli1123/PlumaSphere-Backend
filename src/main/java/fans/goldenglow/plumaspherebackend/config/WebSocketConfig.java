package fans.goldenglow.plumaspherebackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final CorsProperties corsProperties;
    private final WebSocketHandler webSocketHandler;

    @Autowired
    public WebSocketConfig(CorsProperties corsProperties, WebSocketHandler webSocketHandler) {
        this.corsProperties = corsProperties;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
                .setAllowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new));
    }
}
