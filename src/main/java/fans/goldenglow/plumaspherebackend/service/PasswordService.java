package fans.goldenglow.plumaspherebackend.service;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for handling password encoding and verification.
 * Uses Argon2 for secure password hashing and provides methods to generate random passwords.
 */
@Service
public class PasswordService {
    private final Argon2PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    /**
     * Constructs a PasswordService with Argon2 password encoder and secure random generator.
     */
    public PasswordService() {
        this.passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        this.secureRandom = new SecureRandom();
    }

    /**
     * Verifies if the provided password matches the encoded password.
     *
     * @param password        the plain text password to verify
     * @param encodedPassword the encoded password to compare against
     * @return true if the passwords match, false otherwise
     */
    public boolean verifyPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    /**
     * Encodes a plain text password using Argon2.
     *
     * @param password the plain text password to encode
     * @return the encoded password
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Generates a random password, encodes it using Argon2, and returns the encoded password.
     *
     * @return the encoded random password
     */
    public String generateRandomPassword() {
        int BYTE_LENGTH = 32;
        byte[] randomBytes = new byte[BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String password = Base64.getUrlEncoder().encodeToString(randomBytes);
        return passwordEncoder.encode(password);
    }
}
