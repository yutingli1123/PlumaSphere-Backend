package fans.goldenglow.plumaspherebackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for managing Redis operations.
 * Provides methods to set, get, delete keys, and manage sets in Redis.
 */
@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Sets a key-value pair in Redis.
     *
     * @param key   the key to set
     * @param value the value to associate with the key
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Retrieves the value associated with a key from Redis.
     *
     * @param key the key to retrieve the value for
     * @return the value associated with the key, or null if the key does not exist
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Deletes a key from Redis.
     *
     * @param key the key to delete
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Adds a value to a set in Redis.
     *
     * @param key   the key of the set
     * @param value the value to add to the set
     */
    public void addToSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    /**
     * Removes a value from a set in Redis.
     *
     * @param key   the key of the set
     * @param value the value to remove from the set
     */
    public void removeFromSet(String key, String value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    /**
     * Retrieves all members of a set in Redis.
     *
     * @param key the key of the set
     * @return a set of members in the specified Redis set, or null if the key does not exist
     */
    public Set<String> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Retrieves all keys matching a specific pattern from Redis.
     *
     * @param pattern the pattern to match keys against
     * @return a set of keys matching the specified pattern
     */
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * Checks if a key exists in Redis.
     *
     * @param key the key to check for existence
     * @return true if the key exists, false otherwise
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Checks if a value exists in a set in Redis.
     *
     * @param key   the key of the set
     * @param value the value to check for existence in the set
     * @return true if the value exists in the set, false otherwise
     */
    public boolean existsInSet(String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * Gets the size of a set in Redis.
     *
     * @param key the key of the set
     * @return the number of members in the set, or null if the key does not exist
     */
    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }
}
