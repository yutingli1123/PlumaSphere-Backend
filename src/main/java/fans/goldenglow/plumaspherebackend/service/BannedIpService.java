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
        return bannedIpRepository.existsByIpAddressAndExpiresAtAfter(ipAddress, LocalDateTime.now());
    }

    @Transactional
    public void banIp(String ipAddress, String reason) {
        if (isIpBanned(ipAddress)) {
            return;
        }

        BannedIp bannedIp = new BannedIp(ipAddress, reason);
        bannedIpRepository.save(bannedIp);
        log.info("IP {} banned permanently. Reason: {}.",
                ipAddress, reason);
    }

    @Transactional
    public void banIpTemporary(String ipAddress, String reason, LocalDateTime expiresAt) {
        if (isIpBanned(ipAddress)) {
            return;
        }

        BannedIp bannedIp = new BannedIp(ipAddress, reason, expiresAt);
        bannedIpRepository.save(bannedIp);
        log.info("IP {} banned temporarily until {}. Reason: {}.",
                ipAddress, expiresAt, reason);
    }

    @Transactional
    public void unbanIp(String ipAddress) {
        if (!bannedIpRepository.existsByIpAddress(ipAddress)) {
            return;
        }

        bannedIpRepository.deleteByIpAddress(ipAddress);
        log.info("IP {} has been unbanned", ipAddress);
    }

    @Transactional(readOnly = true)
    public Page<BannedIp> getAllBans(Pageable pageable) {
        return bannedIpRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public long countBannedIps() {
        return bannedIpRepository.count();
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredBans() {
        bannedIpRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
        log.debug("Cleaned up expired IP bans");
    }
}