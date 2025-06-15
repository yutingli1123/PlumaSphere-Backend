package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String BANNED_USERNAME = "bannedUser";
    private static final String PENDING_IP_BAN_USERNAME = "pendingIpBanUser";
    private static final String BAN_REASON = "reason";
    
    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Given
        User user = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByUsername(TEST_USERNAME);

        // Then
        assertThat(foundUser)
                .isPresent()
                .get()
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo(TEST_USERNAME);
                    assertThat(u.getPassword()).isEqualTo(TEST_PASSWORD);
                });
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonExistentUser");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // Given
        User user = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByUsername(TEST_USERNAME);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonExistentUser");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByIsBannedTrue_ShouldReturnBannedUsers_WhenBannedUsersExist() {
        // Given
        User bannedUser = new User(BANNED_USERNAME, TEST_PASSWORD);
        bannedUser.ban(BAN_REASON);
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.saveAll(List.of(bannedUser, regularUser));

        PageRequest pageable = PageRequest.of(0, 10);

        // When
        Page<User> page = userRepository.findByIsBannedTrue(pageable);

        // Then
        assertThat(page.getContent())
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(BANNED_USERNAME);
                    assertThat(user.getBanReason()).isEqualTo(BAN_REASON);
                    assertThat(user.getIsBanned()).isTrue();
                });
    }

    @Test
    void findByIsBannedTrue_ShouldReturnEmptyPage_WhenNoBannedUsersExist() {
        // Given
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(regularUser);

        PageRequest pageable = PageRequest.of(0, 10);

        // When
        Page<User> page = userRepository.findByIsBannedTrue(pageable);

        // Then
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findByIsPendingIpBanTrue_ShouldReturnPendingUsers_WhenPendingUsersExist() {
        // Given
        User pendingIpBanUser = new User(PENDING_IP_BAN_USERNAME, TEST_PASSWORD);
        pendingIpBanUser.markForIpBan(BAN_REASON);
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.saveAll(List.of(pendingIpBanUser, regularUser));

        PageRequest pageable = PageRequest.of(0, 10);

        // When
        Page<User> page = userRepository.findByIsPendingIpBanTrue(pageable);

        // Then
        assertThat(page.getContent())
                .hasSize(1)
                .first()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(PENDING_IP_BAN_USERNAME);
                    assertThat(user.getIpBanReason()).isEqualTo(BAN_REASON);
                    assertThat(user.getIsPendingIpBan()).isTrue();
                });
    }

    @Test
    void findByIsPendingIpBanTrue_ShouldReturnEmptyPage_WhenNoPendingUsersExist() {
        // Given
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(regularUser);

        PageRequest pageable = PageRequest.of(0, 10);

        // When
        Page<User> page = userRepository.findByIsPendingIpBanTrue(pageable);

        // Then
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void countByIsBannedTrue_ShouldReturnCorrectCount_WhenBannedUsersExist() {
        // Given
        User bannedUser1 = new User(BANNED_USERNAME + "1", TEST_PASSWORD);
        bannedUser1.ban(BAN_REASON);
        User bannedUser2 = new User(BANNED_USERNAME + "2", TEST_PASSWORD);
        bannedUser2.ban(BAN_REASON);
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.saveAll(List.of(bannedUser1, bannedUser2, regularUser));

        // When
        Long count = userRepository.countByIsBannedTrue();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByIsBannedTrue_ShouldReturnZero_WhenNoBannedUsersExist() {
        // Given
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(regularUser);

        // When
        Long count = userRepository.countByIsBannedTrue();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void countByIsPendingIpBanTrue_ShouldReturnCorrectCount_WhenPendingUsersExist() {
        // Given
        User pendingUser1 = new User(PENDING_IP_BAN_USERNAME + "1", TEST_PASSWORD);
        pendingUser1.markForIpBan(BAN_REASON);
        User pendingUser2 = new User(PENDING_IP_BAN_USERNAME + "2", TEST_PASSWORD);
        pendingUser2.markForIpBan(BAN_REASON);
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.saveAll(List.of(pendingUser1, pendingUser2, regularUser));

        // When
        Long count = userRepository.countByIsPendingIpBanTrue();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByIsPendingIpBanTrue_ShouldReturnZero_WhenNoPendingUsersExist() {
        // Given
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(regularUser);

        // When
        Long count = userRepository.countByIsPendingIpBanTrue();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findUserByBanExpiresAtBefore_ShouldReturnExpiredBans_WhenExpiredBansExist() {
        // Given
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        User expiredBanUser = new User("expiredUser", TEST_PASSWORD);
        expiredBanUser.ban(BAN_REASON);
        expiredBanUser.setBanExpiresAt(pastDate);

        User activeBanUser = new User("activeUser", TEST_PASSWORD);
        activeBanUser.ban(BAN_REASON);
        activeBanUser.setBanExpiresAt(futureDate);

        userRepository.saveAll(List.of(expiredBanUser, activeBanUser));

        // When
        List<User> expiredUsers = userRepository.findUserByBanExpiresAtBefore(LocalDateTime.now());

        // Then
        assertThat(expiredUsers)
                .hasSize(1)
                .first()
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("expiredUser");
                    assertThat(u.getBanReason()).isEqualTo(BAN_REASON);
                    assertThat(u.getBanExpiresAt()).isBefore(LocalDateTime.now());
                });
    }

    @Test
    void findUserByBanExpiresAtBefore_ShouldReturnEmptyList_WhenNoExpiredBansExist() {
        // Given
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        User activeBanUser = new User("activeUser", TEST_PASSWORD);
        activeBanUser.ban(BAN_REASON);
        activeBanUser.setBanExpiresAt(futureDate);
        userRepository.save(activeBanUser);

        // When
        List<User> expiredUsers = userRepository.findUserByBanExpiresAtBefore(LocalDateTime.now());

        // Then
        assertThat(expiredUsers).isEmpty();
    }

    @Test
    void deleteByIdAndRoleIsNot_ShouldDeleteRegularUser_WhenUserIsNotAdmin() {
        // Given
        User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
        regularUser.setRole(UserRoles.REGULAR);
        User savedUser = userRepository.save(regularUser);
        Long userId = savedUser.getId();

        // Verify user exists
        assertThat(userRepository.findById(userId)).isPresent();

        // When
        userRepository.deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);

        // Then
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void deleteByIdAndRoleIsNot_ShouldNotDeleteAdminUser_WhenUserIsAdmin() {
        // Given
        User adminUser = new User("adminUser", TEST_PASSWORD);
        adminUser.setRole(UserRoles.ADMIN);
        User savedUser = userRepository.save(adminUser);
        Long userId = savedUser.getId();

        // Verify user exists
        assertThat(userRepository.findById(userId)).isPresent();

        // When
        userRepository.deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);

        // Then
        assertThat(userRepository.findById(userId)).isPresent();
    }

    @Test
    void deleteByIdAndRoleIsNot_ShouldHandleNonExistentId() {
        // Given
        Long nonExistentId = 99999L;

        // When & Then - Should not throw any exception
        userRepository.deleteByIdAndRoleIsNot(nonExistentId, UserRoles.ADMIN);
    }

    @Test
    void findByUsername_ShouldBeCaseSensitive() {
        // Given
        User user = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByUsername(TEST_USERNAME.toUpperCase());

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByUsername_ShouldBeCaseSensitive() {
        // Given
        User user = new User(TEST_USERNAME, TEST_PASSWORD);
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByUsername(TEST_USERNAME.toUpperCase());

        // Then
        assertThat(exists).isFalse();
    }
}
