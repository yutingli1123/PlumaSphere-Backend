package fans.goldenglow.plumaspherebackend.util;

import com.oblac.nomen.Nomen;

import java.security.SecureRandom;

public class RandomUtil {
    private final static SecureRandom secureRandom = new SecureRandom();

    public static String generateRandomUsername() {
        return Nomen.est().adjective().noun().get() + '_' + secureRandom.nextInt(100, 999);
    }
}
