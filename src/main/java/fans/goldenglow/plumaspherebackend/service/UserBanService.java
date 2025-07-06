package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user bans.
 * Provides methods to ban, unban, check ban status, and manage banned users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBanService {
    private final UserRepository userRepository;

    /**
     * Bans a user permanently with a specified reason.
     *
     * @param id     the ID of the user
     * @param reason the reason of banning the user
     */
    @Transactional
    public void banUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.ban(reason);
        userRepository.save(user);
        log.info("User {} banned permanently. Reason: {}", id, reason);
    }

    /**
     * Temporarily bans a user until a specified expiration time.
     *
     * @param id        the ID of the user
     * @param reason    the reason for the temporary ban
     * @param expiresAt the expiration time of the ban
     */
    @Transactional
    public void banUserTemporary(Long id, String reason, LocalDateTime expiresAt) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.banTemporary(reason, expiresAt);
        userRepository.save(user);
        log.info("User {} banned temporarily until {}. Reason: {}",
                id, expiresAt, reason);
    }

    /**
     * Unbans a user, removing any ban status.
     *
     * @param id the ID of the user
     */
    @Transactional
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.unban();
        userRepository.save(user);
        log.info("User {} has been unbanned", id);
    }

    /**
     * Checks if a user is currently banned.
     *
     * @param id the ID of the user
     * @return true if the user is banned, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isUserBanned(Long id) {
        return userRepository.findById(id)
                .map(User::isCurrentlyBanned)
                .orElse(false);
    }

    /**
     * Retrieves a paginated list of banned users.
     *
     * @param pageable the pagination information
     * @return a page of banned users
     */
    @Transactional(readOnly = true)
    public Page<User> getBannedUsers(Pageable pageable) {
        return userRepository.findByIsBannedTrue(pageable);
    }

    /**
     * Counts the number of banned users.
     *
     * @return the count of banned users
     */
    @Transactional(readOnly = true)
    public Long countBannedUsers() {
        return userRepository.countByIsBannedTrue();
    }

    /**
     * Searches for banned users based on a keyword.
     *
     * @param keyword  Keyword to search for.
     * @param pageable the pagination information
     * @return a page of banned users matching the keyword.
     */
    @Transactional(readOnly = true)
    public Page<User> searchBannedUsersByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchBannedUsersByKeyword(keyword, pageable);
    }

    /**
     * Counts the number of banned users matching the keyword.
     *
     * @param keyword Keyword to search for.
     * @return the count of banned users matching the keyword.
     */
    @Transactional(readOnly = true)
    public Long countBannedUsersByKeyword(String keyword) {
        return userRepository.countBannedUsersByKeyword(keyword);
    }

    /**
     * Retrieves a paginated list of users marked for IP ban.
     *
     * @param pageable the pagination information
     * @return a page of users marked for IP ban
     */
    @Transactional(readOnly = true)
    public Page<User> getMarkedUsers(Pageable pageable) {
        return userRepository.findByIsPendingIpBanTrue(pageable);
    }

    /**
     * Counts the number of users marked for IP ban.
     *
     * @return the count of users marked for IP ban
     */
    @Transactional(readOnly = true)
    public Long countMarkedUsers() {
        return userRepository.countByIsPendingIpBanTrue();
    }

    /**
     * Cleans up expired user bans by unbanning users whose ban has expired.
     * This method is scheduled to run every hour.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredBans() {
        List<User> expiredBannedUsers = userRepository.findUserByBanExpiresAtBefore(LocalDateTime.now());

        for (User user : expiredBannedUsers) {
            user.unban();
            log.info("Automatically unbanned user {} due to ban expiration", user.getId());
        }

        if (!expiredBannedUsers.isEmpty()) {
            userRepository.saveAll(expiredBannedUsers);
            log.info("Cleaned up {} expired user bans", expiredBannedUsers.size());
        }
    }

    /**
     * Searches for pending banned users based on a keyword.
     *
     * @param keyword  Keyword to search for.
     * @param pageable the pagination information
     * @return a page of pending banned users matching the keyword.
     */
    @Transactional(readOnly = true)
    public Page<User> searchPendingBannedUsersByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchPendingBannedUsersByKeyword(keyword, pageable);
    }

    /**
     * Counts the number of pending banned users matching the keyword.
     *
     * @param keyword Keyword to search for.
     * @return the count of pending banned users matching the keyword.
     */
    @Transactional(readOnly = true)
    public Long countPendingBannedUsersByKeyword(String keyword) {
        return userRepository.countPendingBannedUsersByKeyword(keyword);
    }
}