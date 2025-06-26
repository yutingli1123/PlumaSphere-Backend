package fans.goldenglow.plumaspherebackend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Exception thrown when there is an error saving a file.
 * This exception includes the name of the file that could not be saved.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileSaveException extends Throwable {
    private String fileName;
}
