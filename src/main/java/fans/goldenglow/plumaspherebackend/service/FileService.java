package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Service for handling file uploads and downloads.
 * Provides methods to save files, fetch images from URLs, and validate URLs.
 * This service is mainly required by the frontend to upload files
 */
@SuppressWarnings("JvmTaintAnalysis")
@Service
public class FileService {
    private static final String UPLOAD_DIR = "upload";
    private final String accessUrl;

    /**
     * Constructs a FileService with the specified server full address.
     *
     * @param serverFullAddress the full address of the server, used to construct the access URL
     */
    public FileService(@Value("${config.server_full_address}") String serverFullAddress) {
        this.accessUrl = serverFullAddress + "/" + UPLOAD_DIR + "/";
    }

    /**
     * Saves a file uploaded via a multipart request.
     *
     * @param file the file to save
     * @return the URL where the saved file can be accessed
     * @throws FileSaveException if the file cannot be saved
     */
    public String saveFile(MultipartFile file) throws FileSaveException {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) throw new RuntimeException("Failed to create upload directory");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new FileSaveException();
        }

        String extension = FilenameUtils.getExtension(originalFilename);
        String filename = extension.isEmpty() ? UUID.randomUUID().toString() : UUID.randomUUID() + "." + extension;

        File dest = new File(dir, filename);
        try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(dest)) {
            StreamUtils.copy(in, out);
            return accessUrl + filename;
        } catch (Exception e) {
            throw new FileSaveException();
        }
    }

    /**
     * Fetches an image from a given URL and saves it to the upload directory.
     *
     * @param originalURL the URL of the image to fetch
     * @return the URL where the saved image can be accessed
     * @throws FileSaveException if the image cannot be fetched or saved
     */
    public String fetchImage(String originalURL) throws FileSaveException {
        if (!checkURLValidation(originalURL)) throw new FileSaveException();

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) throw new RuntimeException("Failed to create upload directory");
        }

        String extension = FilenameUtils.getExtension(originalURL);
        String filename = extension.isEmpty() ? UUID.randomUUID().toString() : UUID.randomUUID() + "." + extension;

        File dest = new File(dir, filename);
        try (InputStream in = new URI(originalURL).toURL().openStream(); OutputStream out = new FileOutputStream(dest)) {
            StreamUtils.copy(in, out);
            return accessUrl + filename;
        } catch (Exception e) {
            throw new FileSaveException();
        }
    }

    /**
     * Validates a URL to ensure it is well-formed and not a local address.
     *
     * @param url the URL to validate
     * @return true if the URL is valid, false otherwise
     */
    public boolean checkURLValidation(String url) {
        if (url == null || url.isEmpty()) return false;
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) return false;
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) return false;
            return !isLocalAddress(host);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the given host is a local address (loopback, link-local, or site-local).
     *
     * @param host the host to check
     * @return true if the host is a local address, false otherwise
     * @throws UnknownHostException if the host cannot be resolved
     */
    private boolean isLocalAddress(String host) throws UnknownHostException {
        if (host == null || host.isEmpty()) return false;
        InetAddress addr = InetAddress.getByName(host);
        return addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress();
    }
}
