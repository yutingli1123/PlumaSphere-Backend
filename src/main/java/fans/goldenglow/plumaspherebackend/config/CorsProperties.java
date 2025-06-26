package fans.goldenglow.plumaspherebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 * This class is used to load CORS configuration from application properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "config.cors")
public class CorsProperties {
    private List<String> allowedOrigins;
}
