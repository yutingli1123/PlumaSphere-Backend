package fans.goldenglow.plumaspherebackend.service;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {
    private final Argon2PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public PasswordService() {
        this.passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        this.secureRandom = new SecureRandom();
    }

    public boolean verifyPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public String generateRandomPassword() {
        int BYTE_LENGTH = 32;
        byte[] randomBytes = new byte[BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String password = Base64.getUrlEncoder().encodeToString(randomBytes);
        return passwordEncoder.encode(password);
    }
}
