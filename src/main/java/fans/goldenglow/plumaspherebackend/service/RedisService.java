package fans.goldenglow.plumaspherebackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void addToSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public void removeFromSet(String key, String value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    public Set<String> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean existsInSet(String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }
}
