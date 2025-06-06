package fans.goldenglow.plumaspherebackend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileSaveException extends Throwable {
    private final String fileName = null;
}
