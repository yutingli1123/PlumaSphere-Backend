package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1")
public class SystemController {
    private final ConfigService configService;

    @Autowired
    public SystemController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/status")
    public ResponseEntity<HashMap<String, String>> getStatus() {
        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("isInit", configService.get("initialled").orElse("false"));
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/init")
    public ResponseEntity<Boolean> initSystem() {
        // TODO
        return ResponseEntity.internalServerError().build();
    }
}
