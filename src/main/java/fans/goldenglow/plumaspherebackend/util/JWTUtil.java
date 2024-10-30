package fans.goldenglow.plumaspherebackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import fans.goldenglow.plumaspherebackend.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
public class JWTUtil {
    @Autowired
    private SystemConfigService systemConfigService;

    public HashMap<String, String> generateToken(String username) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + 15 * 60 * 1000);
        Date refresh_expire = new Date(now.getTime() + 30 * 60 * 1000);
        HashMap<String, String> result = new HashMap<>();
        String secretKey = systemConfigService.get("secret_key").orElse(null);
        if (secretKey == null) {
            secretKey = systemConfigService.generateSecretKey();
        }
        result.put("token", JWT.create().withIssuer(username).withIssuedAt(now).withExpiresAt(expire).sign(Algorithm.HMAC256(secretKey)));
        result.put("refresh_token", JWT.create().withIssuer(username).withIssuedAt(now).withExpiresAt(refresh_expire).sign(Algorithm.HMAC256(secretKey)));
        return result;
    }
}
