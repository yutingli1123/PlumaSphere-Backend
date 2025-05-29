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

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final BannedIpService bannedIpService;
    private final UserService userService;
    private final UserBanService userBanService;

    @PostMapping("/ban-user")
    public ResponseEntity<String> banUser(@RequestParam String username,
                                          @RequestParam String reason,
                                          @RequestParam(required = false) LocalDateTime expiresAt) {
        try {
            if (expiresAt != null) {
                userBanService.banUserTemporary(username, reason, expiresAt);
                return ResponseEntity.ok("User " + username + " banned temporarily until " + expiresAt);
            } else {
                userBanService.banUser(username, reason, currentAdmin.getId());
                return ResponseEntity.ok("User " + username + " banned permanently");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error banning user: " + e.getMessage());
        }
    }

    @DeleteMapping("/unban-user")
    public ResponseEntity<String> unbanUser(@RequestParam String username) {
        try {
            userBanService.unbanUser(username);
            return ResponseEntity.ok("User " + username + " has been unbanned");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error unbanning user: " + e.getMessage());
        }
    }

    @GetMapping("/banned-users")
    public ResponseEntity<Page<User>> getBannedUsers(Pageable pageable) {
        Page<User> bannedUsers = userBanService.getBannedUsers(pageable);
        return ResponseEntity.ok(bannedUsers);
    }

    @GetMapping("/user-ban-status")
    public ResponseEntity<Boolean> checkUserBanStatus(@RequestParam String username) {
        boolean isBanned = userBanService.isUserBanned(username);
        return ResponseEntity.ok(isBanned);
    }

    @PostMapping("/mark-user-for-ip-ban")
    public ResponseEntity<String> markUserForIpBan(@RequestParam String username,
                                                   @RequestParam String reason,
                                                   @RequestParam(required = false) LocalDateTime expiresAt) {
        try {
            User user = userService.findByUsername(username);

            if (expiresAt != null) {
                user.markForTemporaryIpBan(reason, expiresAt);
            } else {
                user.markForIpBan(reason);
            }

            userService.save(user);
            return ResponseEntity.ok("User " + username + " marked for IP ban. Reason: " + reason);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking user: " + e.getMessage());
        }
    }

    @DeleteMapping("/unmark-user-ip-ban")
    public ResponseEntity<String> unmarkUserIpBan(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            user.clearIpBanMark();
            userService.save(user);
            return ResponseEntity.ok("User " + username + " unmarked for IP ban");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error unmarking user: " + e.getMessage());
        }
    }

    @PostMapping("/ban-ip")
    public ResponseEntity<BannedIp> banIp(@RequestParam String ipAddress,
                                          @RequestParam String reason,
                                          @RequestParam(required = false) LocalDateTime expiresAt) {
        User currentAdmin = userService.getCurrentUser();

        BannedIp bannedIp = expiresAt != null
                ? bannedIpService.banIpTemporary(ipAddress, reason, expiresAt, currentAdmin)
                : bannedIpService.banIp(ipAddress, reason, currentAdmin);

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