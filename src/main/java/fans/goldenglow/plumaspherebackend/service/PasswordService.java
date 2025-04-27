package fans.goldenglow.plumaspherebackend.service;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final Argon2PasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
    }

    public boolean verifyPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
