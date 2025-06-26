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

/**
 * Service for managing banned IP addresses.
 * Provides methods to ban, unban, check if an IP is banned, and clean up expired bans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BannedIpService {
    private final BannedIpRepository bannedIpRepository;

    /**
     * Checks if the given IP address is currently banned.
     *
     * @param ipAddress the IP address to check
     * @return true if the IP is banned, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isIpBanned(String ipAddress) {
        return bannedIpRepository.existsByIpAddressAndExpiresAtAfter(ipAddress, LocalDateTime.now());
    }

    /**
     * Bans an IP address permanently with a reason.
     *
     * @param ipAddress the IP address to ban
     * @param reason    the reason for the ban
     */
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

    /**
     * Bans an IP address temporarily with a reason and expiration time.
     *
     * @param ipAddress the IP address to ban
     * @param reason the reason for the ban
     * @param expiresAt the time when the ban expires
     */
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

    /**
     * Unbans an IP address if it is currently banned.
     *
     * @param ipAddress the IP address to unban
     */
    @Transactional
    public void unbanIp(String ipAddress) {
        if (!bannedIpRepository.existsByIpAddress(ipAddress)) {
            return;
        }

        bannedIpRepository.deleteByIpAddress(ipAddress);
        log.info("IP {} has been unbanned", ipAddress);
    }

    /**
     * Retrieves a paginated list of all banned IP addresses.
     *
     * @param pageable the pagination information
     * @return a page of banned IP addresses
     */
    @Transactional(readOnly = true)
    public Page<BannedIp> getAllBans(Pageable pageable) {
        return bannedIpRepository.findAll(pageable);
    }

    /**
     * Counts the total number of banned IP addresses.
     *
     * @return the count of banned IP addresses
     */
    @Transactional(readOnly = true)
    public long countBannedIps() {
        return bannedIpRepository.count();
    }

    /**
     * Cleans up expired IP bans by deleting all entries that have expired.
     * This method is scheduled to run every hour.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredBans() {
        bannedIpRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
        log.debug("Cleaned up expired IP bans");
    }
}