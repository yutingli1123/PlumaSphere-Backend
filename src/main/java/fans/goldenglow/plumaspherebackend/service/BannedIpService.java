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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannedIpService {
    private final BannedIpRepository bannedIpRepository;

    @Transactional(readOnly = true)
    public boolean isIpBanned(String ipAddress) {
        return bannedIpRepository.existsByIpAddressAndExpiresAtAfter(ipAddress, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    protected Optional<BannedIp> checkIfIpAlreadyBanned(String ipAddress) {
        if (isIpBanned(ipAddress)) {
            log.warn("IP {} is already banned", ipAddress);
            return bannedIpRepository.findByIpAddressAndExpiresAtAfter(ipAddress, LocalDateTime.now());
        }
        return Optional.empty();
    }

    @Transactional
    public BannedIp banIp(String ipAddress, String reason) {
        Optional<BannedIp> existingBan = checkIfIpAlreadyBanned(ipAddress);
        if (existingBan.isPresent()) {
            return existingBan.get();
        }

        BannedIp bannedIp = new BannedIp(ipAddress, reason);
        BannedIp saved = bannedIpRepository.save(bannedIp);
        log.info("IP {} banned permanently. Reason: {}.",
                ipAddress, reason);
        return saved;
    }

    @Transactional
    public BannedIp banIpTemporary(String ipAddress, String reason, LocalDateTime expiresAt) {
        Optional<BannedIp> existingBan = checkIfIpAlreadyBanned(ipAddress);
        if (existingBan.isPresent()) {
            return existingBan.get();
        }

        BannedIp bannedIp = new BannedIp(ipAddress, reason, expiresAt);
        BannedIp saved = bannedIpRepository.save(bannedIp);
        log.info("IP {} banned temporarily until {}. Reason: {}.",
                ipAddress, expiresAt, reason);
        return saved;
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