package fans.goldenglow.plumaspherebackend.exceptions;

import lombok.Getter;

@Getter
public class FileSaveException extends Throwable {
    private final String fileName;

    public FileSaveException(String fileName) {
        this.fileName = fileName;
    }
}
