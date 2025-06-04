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
    public void banUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.ban(reason);
        userRepository.save(user);
        log.info("User {} banned permanently. Reason: {}", id, reason);
    }

    @Transactional
    public void banUserTemporary(Long id, String reason, LocalDateTime expiresAt) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.banTemporary(reason, expiresAt);
        userRepository.save(user);
        log.info("User {} banned temporarily until {}. Reason: {}",
                id, expiresAt, reason);
    }

    @Transactional
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.unban();
        userRepository.save(user);
        log.info("User {} has been unbanned", id);
    }

    @Transactional(readOnly = true)
    public boolean isUserBanned(Long id) {
        return userRepository.findById(id)
                .map(User::isCurrentlyBanned)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Page<User> getBannedUsers(Pageable pageable) {
        return userRepository.findByIsBannedTrueOrderByBannedAtDesc(pageable);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredBans() {
        List<User> expiredBannedUsers = userRepository.findExpiredBannedUsers(LocalDateTime.now());

        for (User user : expiredBannedUsers) {
            user.unban();
            log.info("Automatically unbanned user {} due to ban expiration", user.getId());
        }

        if (!expiredBannedUsers.isEmpty()) {
            userRepository.saveAll(expiredBannedUsers);
            log.info("Cleaned up {} expired user bans", expiredBannedUsers.size());
        }
    }
}