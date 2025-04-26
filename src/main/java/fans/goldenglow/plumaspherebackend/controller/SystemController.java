package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.entity.Config;
import fans.goldenglow.plumaspherebackend.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SystemController {
    private final ConfigService configService;

    @Autowired
    public SystemController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/status")
    public ResponseEntity<List<Config>> getStatus(JwtAuthenticationToken token) {
        if (token != null) {
            Map<String, Object> attributes = token.getTokenAttributes();
            if (attributes.containsKey("scope") && ((String) attributes.get("scope")).contains("admin")) {
                return ResponseEntity.ok(configService.getAll());
            }
        }
        return ResponseEntity.ok(configService.getAllPublic());
    }

    @PostMapping("/init")
    public ResponseEntity<Boolean> initSystem() {
        // TODO
        return ResponseEntity.internalServerError().build();
    }
}
