package fans.goldenglow.plumaspherebackend.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisTestConfiguration {
    private final RedisServer redisServer;

    private final String redisHost;
    private final int redisPort;

    public EmbeddedRedisTestConfiguration(RedisProperties redisProperties) throws IOException {
        redisHost = redisProperties.getRedisHost();
        redisPort = redisProperties.getRedisPort();
        redisServer = new RedisServer(redisPort);
    }

    @PostConstruct
    public void startRedis() throws IOException {
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        redisServer.stop();
    }

    @Bean
    @Primary
    public RedisConnectionFactory testRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }
}
