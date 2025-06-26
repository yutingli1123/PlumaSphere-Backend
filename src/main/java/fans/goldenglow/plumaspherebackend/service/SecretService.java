package fans.goldenglow.plumaspherebackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/**
 * Service for managing JWT secret keys.
 * Provides methods to generate and retrieve a secure secret key for JWT signing.
 */
@Slf4j
@Service
public class SecretService {
    private SecretKey jwtSecret;

    /**
     * Generates a secure secret key for JWT signing using HmacSHA256 algorithm.
     * The key is generated with a size of 256 bits.
     *
     * @return a SecretKey instance
     */
    private SecretKey generateSecret() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm", e);
            throw new RuntimeException("Failed to generate JWT secret key", e);
        }
    }

    /**
     * Get the secret key used by the system.
     * If the secret key is not generated yet, generate it first.
     *
     * @return the secret key used by the system
     */
    public synchronized SecretKey getSecret() {
        if (jwtSecret == null) {
            jwtSecret = generateSecret();
        }
        return jwtSecret;
    }
}