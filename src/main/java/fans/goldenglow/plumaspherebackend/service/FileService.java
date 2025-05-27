package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

@Service
public class FileService {
    private static final String UPLOAD_DIR = "upload";
    private final String accessUrl;

    public FileService(@Value("${config.server_full_address}") String serverFullAddress) {
        this.accessUrl = serverFullAddress + "/" + UPLOAD_DIR + "/";
    }

    public String saveFile(MultipartFile file) throws FileSaveException {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) throw new RuntimeException("Failed to create upload directory");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) return null;
        String ext = getExtFromString(originalFilename);
        if (ext == null) {
            throw new FileSaveException(originalFilename);
        }
        String filename = UUID.randomUUID() + "." + ext;
        File dest = new File(dir, filename);
        try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(dest)) {
            StreamUtils.copy(in, out);
            return accessUrl + filename;
        } catch (Exception e) {
            throw new FileSaveException(originalFilename);
        }
    }

    public String fetchImage(String originalURL) throws FileSaveException {
        String ext = getExtFromString(originalURL);
        if (ext == null) throw new FileSaveException(null);
        String filename = UUID.randomUUID() + "." + ext;
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) throw new RuntimeException("Failed to create upload directory");
        }
        File dest = new File(dir, filename);
        try (InputStream in = new URI(originalURL).toURL().openStream();
             OutputStream out = new FileOutputStream(dest)) {
            StreamUtils.copy(in, out);
            return accessUrl + filename;
        } catch (Exception e) {
            throw new FileSaveException(null);
        }
    }

    private String getExtFromString(String s) {
        if (s == null || s.isEmpty()) return null;
        int lastDotIndex = s.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == s.length() - 1) return null;
        String ext = s.substring(lastDotIndex + 1).toLowerCase();
        if (!ext.matches("^[a-zA-Z0-9]+$")) return null;
        return ext;
    }
}
