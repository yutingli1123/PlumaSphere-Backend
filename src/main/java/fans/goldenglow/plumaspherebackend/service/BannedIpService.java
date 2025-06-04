package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import fans.goldenglow.plumaspherebackend.repository.BannedIpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannedIpService {
    private final BannedIpRepository bannedIpRepository;

    @Transactional(readOnly = true)
    public boolean isIpBanned(String ipAddress) {
        boolean isBanned = bannedIpRepository.findActiveBanByIp(ipAddress, LocalDateTime.now()).isPresent();
        if (isBanned) {
            log.info("Blocked request from banned IP: {}", ipAddress);
        }
        return isBanned;
    }

    @Transactional
    public BannedIp banIp(String ipAddress, String reason) {
        if (isIpBanned(ipAddress)) {
            log.warn("IP {} is already banned", ipAddress);
            return bannedIpRepository.findActiveBanByIp(ipAddress, LocalDateTime.now()).orElse(null);
        }

        BannedIp bannedIp = new BannedIp(ipAddress, reason);
        BannedIp saved = bannedIpRepository.save(bannedIp);
        log.info("IP {} banned permanently. Reason: {}.",
                ipAddress, reason);
        return saved;
    }

    @Transactional
    public BannedIp banIpTemporary(String ipAddress, String reason, LocalDateTime expiresAt) {
        if (isIpBanned(ipAddress)) {
            log.warn("IP {} is already banned", ipAddress);
            return bannedIpRepository.findActiveBanByIp(ipAddress, LocalDateTime.now()).orElse(null);
        }

        BannedIp bannedIp = new BannedIp(ipAddress, reason, expiresAt);
        BannedIp saved = bannedIpRepository.save(bannedIp);
        log.info("IP {} banned temporarily until {}. Reason: {}.",
                ipAddress, expiresAt, reason);
        return saved;
    }

    @Transactional
    public void unbanIp(String ipAddress) {
        bannedIpRepository.findActiveBanByIp(ipAddress, LocalDateTime.now())
                .ifPresent(ban -> {
                    ban.setIsActive(false);
                    log.info("IP {} has been unbanned", ipAddress);
                });
    }

    public Page<BannedIp> getAllActiveBans(Pageable pageable) {
        return bannedIpRepository.findByIsActiveTrueOrderByBannedAtDesc(pageable);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredBans() {
        bannedIpRepository.deactivateExpiredBans(LocalDateTime.now());
        log.debug("Cleaned up expired IP bans");
    }
}