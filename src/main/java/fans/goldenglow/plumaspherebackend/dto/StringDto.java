package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for a simple string value.
 * This class is used to send a single string value, such as a message or identifier.
 */
@Data
@AllArgsConstructor
public class StringDto {
    private String value;
}
