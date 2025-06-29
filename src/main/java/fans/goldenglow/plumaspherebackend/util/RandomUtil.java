package fans.goldenglow.plumaspherebackend.util;

import com.oblac.nomen.Nomen;

import java.security.SecureRandom;

/**
 * Utility class for generating random usernames.
 * This class uses the Nomen library to create a random username
 * composed of an adjective and a noun, followed by a random number.
 */
public class RandomUtil {
    // SecureRandom instance for generating random numbers
    private final static SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a random username.
     * The username consists of an adjective and a noun from the Nomen library,
     * followed by a random number between 100 and 999.
     *
     * @return A randomly generated username.
     */
    public static String generateRandomUsername() {
        return Nomen.est().adjective().noun().get() + '_' + secureRandom.nextInt(100, 999);
    }
}
