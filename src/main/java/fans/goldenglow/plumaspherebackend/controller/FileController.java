package fans.goldenglow.plumaspherebackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/file")
public class FileController {
    private static final String UPLOAD_DIR = "upload";
    private final String accessUrl;

    public FileController(@Value("${config.server_full_address}") String serverFullAddress) {
        this.accessUrl = serverFullAddress + "/" + UPLOAD_DIR + "/";
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(@RequestParam("file[]") MultipartFile[] files) {
        List<String> errFiles = new ArrayList<>();
        Map<String, String> succMap = new HashMap<>();
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) continue;
            String ext = getExtFromString(originalFilename);
            if (ext == null) {
                errFiles.add(originalFilename);
                continue;
            }
            String filename = UUID.randomUUID() + "." + ext;
            File dest = new File(dir, filename);
            try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(dest)) {
                StreamUtils.copy(in, out);
                succMap.put(filename, accessUrl + filename);
            } catch (Exception e) {
                errFiles.add(originalFilename);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("errFiles", errFiles);
        data.put("succMap", succMap);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "");
        result.put("code", 0);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchImage(@RequestBody Map<String, String> body) {
        String originalURL = body.get("url");
        String ext = getExtFromString(originalURL);
        String filename = UUID.randomUUID() + "." + ext;
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        File dest = new File(dir, filename);
        String localUrl = accessUrl + filename;
        try (InputStream in = new URI(originalURL).toURL().openStream(); OutputStream out = new FileOutputStream(dest)) {
            StreamUtils.copy(in, out);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("msg", "download image failed");
            result.put("code", 1);
            result.put("data", Map.of("originalURL", originalURL, "url", ""));
            return ResponseEntity.ok(result);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("originalURL", originalURL);
        data.put("url", localUrl);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "");
        result.put("code", 0);
        result.put("data", data);
        return ResponseEntity.ok(result);
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
