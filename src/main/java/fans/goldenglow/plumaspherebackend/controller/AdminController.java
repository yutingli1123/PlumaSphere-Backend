package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.BannedIpService;
import fans.goldenglow.plumaspherebackend.service.UserBanService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final BannedIpService bannedIpService;
    private final UserService userService;
    private final UserBanService userBanService;

    @PostMapping("/ban-user")
    public ResponseEntity<String> banUser(@RequestParam Long id,
                                          @RequestParam String reason,
                                          @RequestParam(required = false) LocalDateTime expiresAt) {
        try {
            if (expiresAt != null) {
                userBanService.banUserTemporary(id, reason, expiresAt);
                return ResponseEntity.ok("User " + id + " banned temporarily until " + expiresAt);
            } else {
                userBanService.banUser(id, reason);
                return ResponseEntity.ok("User " + id + " banned permanently");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error banning user: " + e.getMessage());
        }
    }

    @DeleteMapping("/unban-user")
    public ResponseEntity<String> unbanUser(@RequestParam Long id) {
        try {
            userBanService.unbanUser(id);
            return ResponseEntity.ok("User " + id + " has been unbanned");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error unbanning user: " + e.getMessage());
        }
    }

    @GetMapping("/banned-users")
    public ResponseEntity<Page<User>> getBannedUsers(Pageable pageable) {
        return ResponseEntity.ok(userBanService.getBannedUsers(pageable));
    }

    @GetMapping("/user-ban-status")
    public ResponseEntity<Boolean> checkUserBanStatus(@RequestParam Long id) {
        return ResponseEntity.ok(userBanService.isUserBanned(id));
    }

    @PostMapping("/mark-user-for-ip-ban")
    public ResponseEntity<String> markUserForIpBan(@RequestParam Long id,
                                                   @RequestParam String reason,
                                                   @RequestParam(required = false) LocalDateTime expiresAt) {
        try {
            Optional<User> user = userService.findById(id);

            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found with ID: " + id);
            }

            User userEntity = user.get();

            if (expiresAt != null) {
                userEntity.markForTemporaryIpBan(reason, expiresAt);
            } else {
                userEntity.markForIpBan(reason);
            }

            userService.save(userEntity);
            return ResponseEntity.ok("User " + id + " marked for IP ban. Reason: " + reason);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking user: " + e.getMessage());
        }
    }

    @DeleteMapping("/unmark-user-ip-ban")
    public ResponseEntity<String> unmarkUserIpBan(@RequestParam Long id) {
        try {
            Optional<User> user = userService.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found with ID: " + id);
            }
            User userEntity = user.get();
            userEntity.clearIpBanMark();
            userService.save(userEntity);
            return ResponseEntity.ok("User " + id + " unmarked for IP ban");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error unmarking user: " + e.getMessage());
        }
    }

    @PostMapping("/ban-ip")
    public ResponseEntity<BannedIp> banIp(@RequestParam String ipAddress,
                                          @RequestParam String reason,
                                          @RequestParam(required = false) LocalDateTime expiresAt) {
        BannedIp bannedIp = expiresAt != null
                ? bannedIpService.banIpTemporary(ipAddress, reason, expiresAt)
                : bannedIpService.banIp(ipAddress, reason);

        return ResponseEntity.ok(bannedIp);
    }

    @DeleteMapping("/unban-ip")
    public ResponseEntity<Void> unbanIp(@RequestParam String ipAddress) {
        bannedIpService.unbanIp(ipAddress);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/banned-ips")
    public ResponseEntity<Page<BannedIp>> getBannedIps(Pageable pageable) {
        Page<BannedIp> bannedIps = bannedIpService.getAllActiveBans(pageable);
        return ResponseEntity.ok(bannedIps);
    }
}