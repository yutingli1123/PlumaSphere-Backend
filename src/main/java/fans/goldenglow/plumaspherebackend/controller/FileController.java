package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import fans.goldenglow.plumaspherebackend.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(@RequestParam("file[]") MultipartFile[] files) {
        List<String> errFiles = new ArrayList<>();
        Map<String, String> succMap = new HashMap<>();
        for (MultipartFile file : files) {
            try {
                String accessUrl = fileService.saveFile(file);
                if (accessUrl == null) continue;
                succMap.put(file.getOriginalFilename(), accessUrl);
            } catch (FileSaveException e) {
                errFiles.add(e.getFileName());
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
        String localUrl;
        try {
            localUrl = fileService.fetchImage(originalURL);
        } catch (Exception | FileSaveException e) {
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


}
