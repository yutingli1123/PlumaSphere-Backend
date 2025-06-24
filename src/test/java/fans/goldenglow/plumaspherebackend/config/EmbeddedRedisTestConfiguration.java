package fans.goldenglow.plumaspherebackend.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
@Slf4j
public class EmbeddedRedisTestConfiguration {
    private static RedisServer redisServer;

    private final String redisHost;
    private final int redisPort;

    public EmbeddedRedisTestConfiguration(RedisProperties redisProperties) {
        this.redisHost = redisProperties.getRedisHost();
        this.redisPort = redisProperties.getRedisPort();
    }

    @PostConstruct
    public synchronized void startRedis() throws IOException {
        if (redisServer == null) {
            redisServer = new RedisServer(redisPort);
            redisServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (redisServer != null) {
                    try {
                        redisServer.stop();
                    } catch (Exception e) {
                        log.error("Redis server stop failed.", e);
                    }
                }
            }));
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory testRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }
}
