package fans.goldenglow.plumaspherebackend.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Base mapper interface for common mapping operations.
 * This interface provides a default method to convert LocalDateTime to ZonedDateTime.
 */
public interface BaseMapper {
    default ZonedDateTime map(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(ZoneId.systemDefault());
    }
}
