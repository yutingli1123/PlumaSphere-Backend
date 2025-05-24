package fans.goldenglow.plumaspherebackend.constant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Data
@AllArgsConstructor
public class AvatarColor {
    // Red
    public static final AvatarColor CARMINE = new AvatarColor("#960018");
    public static final AvatarColor CARDINAL = new AvatarColor("#C51E3A");
    public static final AvatarColor COQUELICOT = new AvatarColor("#FF3800");
    // Orange
    public static final AvatarColor AEROSPACE_ORANGE = new AvatarColor("#FF4F00");
    // Green
    public static final AvatarColor CAL_POLY_GREEN = new AvatarColor("#FF4F00");
    public static final AvatarColor DARTMOUTH_GREEN = new AvatarColor("#00693E");
    public static final AvatarColor MYRTLE_GREEN = new AvatarColor("#317873");
    // Blue
    public static final AvatarColor BERKELEY_BLUE = new AvatarColor("#003262");
    public static final AvatarColor DELFT_BLUE = new AvatarColor("#1F305E");
    public static final AvatarColor INDIGO_BLUE = new AvatarColor("#00416A");
    public static final AvatarColor SPACE_CADET = new AvatarColor("#1E2952");
    // Purple
    public static final AvatarColor IRIS = new AvatarColor("#5A4FCF");
    // Black
    public static final AvatarColor OUTER_SPACE = new AvatarColor("#414A4C");
    public static final AvatarColor RAISIN_BLACK = new AvatarColor("#242124");
    private static final Random RANDOM = new Random();
    private static final List<AvatarColor> AVATAR_COLORS = Arrays.asList(
            CARMINE, CARDINAL, COQUELICOT, AEROSPACE_ORANGE, CAL_POLY_GREEN,
            DARTMOUTH_GREEN, MYRTLE_GREEN, BERKELEY_BLUE, DELFT_BLUE, INDIGO_BLUE,
            SPACE_CADET, IRIS, OUTER_SPACE, RAISIN_BLACK
    );
    private final String hex;

    public static AvatarColor getRandomColor() {
        return AVATAR_COLORS.get(RANDOM.nextInt(AVATAR_COLORS.size()));
    }

    public static List<AvatarColor> values() {
        return AVATAR_COLORS;
    }

}
