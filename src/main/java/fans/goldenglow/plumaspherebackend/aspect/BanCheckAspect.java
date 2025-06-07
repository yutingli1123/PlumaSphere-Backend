package fans.goldenglow.plumaspherebackend.aspect;

import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.service.BannedIpService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class BanCheckAspect {

    private final UserService userService;
    private final BannedIpService bannedIpService;

    @Before("@annotation(fans.goldenglow.plumaspherebackend.annotation.CheckUserBan)")
    public void checkUserBan(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtToken) {
            Long userId = extractUserIdFromJwt(jwtToken);

            if (userId != null) {
                try {
                    Optional<User> userOptional = userService.findById(userId);

                    if (userOptional.isEmpty()) {
                        log.warn("User with ID {} not found, cannot check ban status", userId);
                        return;
                    }

                    User user = userOptional.get();

                    if (user.isBanExpired()) {
                        user.unban();
                        userService.save(user);
                        log.info("User {} (ID: {}) ban has expired and been automatically lifted", user.getUsername(), userId);
                    }

                    boolean isUserBanned = false;
                    String banMessage = null;

                    if (user.isCurrentlyBanned()) {
                        log.info("Blocked banned user {} (ID: {}) from accessing {}",
                                user.getUsername(), userId, joinPoint.getSignature().getName());

                        banMessage = user.getBanExpiresAt() != null
                                ? String.format("Account banned until %s. Reason: %s", user.getBanExpiresAt(), user.getBanReason())
                                : String.format("Account permanently banned. Reason: %s", user.getBanReason());

                        isUserBanned = true;
                    }

                    if (user.getIsPendingIpBan()) {
                        String clientIp = getClientIpAddress();
                        if (clientIp == null) {
                            log.warn("Could not determine client IP address for user ID {}", userId);
                        } else {
                            log.info("Collecting IP {} for user {} (ID: {}) and adding to ban list. Reason: {}",
                                    clientIp, user.getUsername(), userId, user.getIpBanReason());

                            if (user.getIpBanExpiresAt() != null) {
                                bannedIpService.banIpTemporary(clientIp, user.getIpBanReason(),
                                        user.getIpBanExpiresAt());
                            } else {
                                bannedIpService.banIp(clientIp, user.getIpBanReason());
                            }

                            user.clearIpBanMark();
                            userService.save(user);

                            isUserBanned = true;
                            banMessage = "Your IP has been banned: " + user.getIpBanReason();
                        }
                    }

                    if (isUserBanned) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, banMessage);
                    }

                } catch (ResponseStatusException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("Error checking user ban status for user ID {}: {}", userId, e.getMessage());
                }
            }
        }
    }

    @Before("@annotation(fans.goldenglow.plumaspherebackend.annotation.CheckIpBan)")
    public void checkIpBan(JoinPoint joinPoint) {
        String clientIp = getClientIpAddress();

        if (clientIp == null) {
            log.warn("Could not determine client IP address for request to {}", joinPoint.getSignature().getName());
            return;
        }

        if (bannedIpService.isIpBanned(clientIp)) {
            log.info("Blocked request from banned IP: {} accessing {}", clientIp, joinPoint.getSignature().getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your IP address is banned.");
        }
    }

    private Long extractUserIdFromJwt(JwtAuthenticationToken jwtToken) {
        try {
            return Long.parseLong(jwtToken.getToken().getSubject());
        } catch (NumberFormatException e) {
            log.error("Error parsing user ID from JWT subject: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from JWT: {}", e.getMessage());
            return null;
        }
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return null;
    }
}