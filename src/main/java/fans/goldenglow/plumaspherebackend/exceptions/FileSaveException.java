package fans.goldenglow.plumaspherebackend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileSaveException extends Throwable {
    private String fileName;
}
