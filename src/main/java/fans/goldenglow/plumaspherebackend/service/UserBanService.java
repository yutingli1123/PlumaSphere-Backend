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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBanService {

    private final UserRepository userRepository;

    @Transactional
    public void banUser(String username, String reason, Long adminId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.ban(reason);
        userRepository.save(user);
        log.info("User {} banned permanently by admin {}. Reason: {}", username, adminId, reason);
    }

    @Transactional
    public void banUserTemporary(String username, String reason, LocalDateTime expiresAt) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.banTemporary(reason, expiresAt);
        userRepository.save(user);
        log.info("User {} banned temporarily until {}. Reason: {}",
                username, expiresAt, reason);
    }

    @Transactional
    public void unbanUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.unban();
        userRepository.save(user);
        log.info("User {} has been unbanned", username);
    }

    public boolean isUserBanned(String username) {
        return userRepository.findByUsername(username)
                .map(User::isCurrentlyBanned)
                .orElse(false);
    }

    public Page<User> getBannedUsers(Pageable pageable) {
        return userRepository.findByIsBannedTrueOrderByBannedAtDesc(pageable);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredBans() {
        List<User> expiredBannedUsers = userRepository.findExpiredBannedUsers(LocalDateTime.now());

        for (User user : expiredBannedUsers) {
            user.unban();
            log.info("Automatically unbanned user {} due to ban expiration", user.getUsername());
        }

        if (!expiredBannedUsers.isEmpty()) {
            userRepository.saveAll(expiredBannedUsers);
            log.info("Cleaned up {} expired user bans", expiredBannedUsers.size());
        }
    }
}