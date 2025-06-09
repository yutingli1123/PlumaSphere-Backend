package fans.goldenglow.plumaspherebackend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RedisProperties {
    private final String redisHost;
    private final int redisPort;

    public RedisProperties(@Value("${spring.data.redis.host}") String redisHost,
                           @Value("${spring.data.redis.port}") int redisPort) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

}