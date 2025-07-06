package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("UserRepository Tests")
public class UserRepositoryTest {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String BANNED_USERNAME = "bannedUser";
    private static final String PENDING_IP_BAN_USERNAME = "pendingIpBanUser";
    private static final String BAN_REASON = "Test ban reason";
    
    private final UserRepository userRepository;
    private final TestEntityManager entityManager;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository, TestEntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Nested
    @DisplayName("Username Operations")
    class UsernameOperationsTests {

        @Test
        @DisplayName("Should find user by username when user exists")
        void findByUsername_ShouldReturnUser_WhenUserExists() {
            // Given
            User user = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(user);

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
        @DisplayName("Should return empty when user does not exist")
        void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
            // When
            Optional<User> foundUser = userRepository.findByUsername("nonExistentUser");

            // Then
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should return true when user exists")
        void existsByUsername_ShouldReturnTrue_WhenUserExists() {
            // Given
            User user = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(user);

            // When
            boolean exists = userRepository.existsByUsername(TEST_USERNAME);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when user does not exist")
        void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
            // When
            boolean exists = userRepository.existsByUsername("nonExistentUser");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should be case sensitive for username lookup")
        void findByUsername_ShouldBeCaseSensitive() {
            // Given
            User user = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(user);

            // When
            Optional<User> foundUser = userRepository.findByUsername(TEST_USERNAME.toUpperCase());

            // Then
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should be case sensitive for username existence check")
        void existsByUsername_ShouldBeCaseSensitive() {
            // Given
            User user = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(user);

            // When
            boolean exists = userRepository.existsByUsername(TEST_USERNAME.toUpperCase());

            // Then
            assertThat(exists).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"very_long_username_that_might_cause_issues", "user@domain.com", "user123", "single"})
        @DisplayName("Should handle various username formats")
        void findByUsername_ShouldHandleVariousFormats(String username) {
            // Given
            User user = new User(username, TEST_PASSWORD);
            entityManager.persistAndFlush(user);

            // When
            Optional<User> foundUser = userRepository.findByUsername(username);

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should handle null username gracefully in findByUsername")
        void findByUsername_ShouldHandleNullUsername() {
            // When & Then - Repository should handle null input gracefully
            Optional<User> foundUser = userRepository.findByUsername(null);
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Should handle null username gracefully in existsByUsername")
        void existsByUsername_ShouldHandleNullUsername() {
            // When & Then - Repository should handle null input gracefully
            boolean exists = userRepository.existsByUsername(null);
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should reject whitespace-only username")
        void findByUsername_ShouldRejectWhitespaceUsername() {
            // Given
            String whitespaceUsername = "   ";
            User user = new User(whitespaceUsername, TEST_PASSWORD);

            // When & Then
            assertThatThrownBy(() -> entityManager.persistAndFlush(user))
                    .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
                    .hasMessageContaining("Username cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("Ban Operations")
    class BanOperationsTests {

        @Test
        @DisplayName("Should find banned users when banned users exist")
        void findByIsBannedTrue_ShouldReturnBannedUsers_WhenBannedUsersExist() {
            // Given
            User bannedUser = new User(BANNED_USERNAME, TEST_PASSWORD);
            bannedUser.ban(BAN_REASON);
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persist(bannedUser);
            entityManager.persist(regularUser);
            entityManager.flush();

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
        @DisplayName("Should return empty page when no banned users exist")
        void findByIsBannedTrue_ShouldReturnEmptyPage_WhenNoBannedUsersExist() {
            // Given
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(regularUser);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByIsBannedTrue(pageable);

            // Then
            assertThat(page.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count banned users correctly when banned users exist")
        void countByIsBannedTrue_ShouldReturnCorrectCount_WhenBannedUsersExist() {
            // Given
            User bannedUser1 = new User(BANNED_USERNAME + "1", TEST_PASSWORD);
            bannedUser1.ban(BAN_REASON);
            User bannedUser2 = new User(BANNED_USERNAME + "2", TEST_PASSWORD);
            bannedUser2.ban(BAN_REASON);
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);

            entityManager.persist(bannedUser1);
            entityManager.persist(bannedUser2);
            entityManager.persist(regularUser);
            entityManager.flush();

            // When
            Long count = userRepository.countByIsBannedTrue();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle pagination correctly for banned users")
        void findByIsBannedTrue_ShouldHandlePagination() {
            // Given - Create multiple banned users
            for (int i = 0; i < 5; i++) {
                User bannedUser = new User(BANNED_USERNAME + i, TEST_PASSWORD);
                bannedUser.ban(BAN_REASON + " " + i);
                entityManager.persist(bannedUser);
            }
            entityManager.flush();

            // When - Request first page with size 2
            PageRequest pageable = PageRequest.of(0, 2);
            Page<User> page = userRepository.findByIsBannedTrue(pageable);

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(5);
            assertThat(page.getTotalPages()).isEqualTo(3);
            assertThat(page.hasNext()).isTrue();
            assertThat(page.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("Should handle large dataset for banned users count")
        void countByIsBannedTrue_ShouldHandleLargeDataset() {
            // Given - Create many users (some banned, some not)
            int totalUsers = 100;
            int bannedUsers = 30;

            for (int i = 0; i < totalUsers; i++) {
                User user = new User("user" + i, TEST_PASSWORD);
                if (i < bannedUsers) {
                    user.ban(BAN_REASON);
                }
                entityManager.persist(user);
            }
            entityManager.flush();

            // When
            Long count = userRepository.countByIsBannedTrue();

            // Then
            assertThat(count).isEqualTo(bannedUsers);
        }

        @Test
        @DisplayName("Should find users with expired bans")
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

            entityManager.persist(expiredBanUser);
            entityManager.persist(activeBanUser);
            entityManager.flush();

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
        @DisplayName("Should return zero count when no banned users exist")
        void countByIsBannedTrue_ShouldReturnZero_WhenNoBannedUsersExist() {
            // Given
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(regularUser);

            // When
            Long count = userRepository.countByIsBannedTrue();

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should find expired bans with precise time handling")
        void findUserByBanExpiresAtBefore_ShouldHandlePreciseTime() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneSecondAgo = now.minusSeconds(1);
            LocalDateTime oneSecondLater = now.plusSeconds(1);

            User expiredUser = new User("expiredUser", TEST_PASSWORD);
            expiredUser.ban(BAN_REASON);
            expiredUser.setBanExpiresAt(oneSecondAgo);

            User notExpiredUser = new User("notExpiredUser", TEST_PASSWORD);
            notExpiredUser.ban(BAN_REASON);
            notExpiredUser.setBanExpiresAt(oneSecondLater);

            entityManager.persist(expiredUser);
            entityManager.persist(notExpiredUser);
            entityManager.flush();

            // When
            List<User> expiredUsers = userRepository.findUserByBanExpiresAtBefore(now);

            // Then
            assertThat(expiredUsers)
                    .hasSize(1)
                    .first()
                    .satisfies(u -> assertThat(u.getUsername()).isEqualTo("expiredUser"));
        }

        @Test
        @DisplayName("Should handle null ban expiry date query gracefully")
        void findUserByBanExpiresAtBefore_ShouldHandleNullDate() {
            // Given
            User bannedUser = new User("bannedUser", TEST_PASSWORD);
            bannedUser.ban(BAN_REASON);
            entityManager.persistAndFlush(bannedUser);

            // When - Query with null should be handled gracefully
            List<User> users = userRepository.findUserByBanExpiresAtBefore(null);

            // Then - Should return empty list or handle gracefully
            assertThat(users).isNotNull();
        }
    }

    @Nested
    @DisplayName("IP Ban Operations")
    class IPBanOperationsTests {

        @Test
        @DisplayName("Should find pending IP ban users when they exist")
        void findByIsPendingIpBanTrue_ShouldReturnPendingUsers_WhenPendingUsersExist() {
            // Given
            User pendingIpBanUser = new User(PENDING_IP_BAN_USERNAME, TEST_PASSWORD);
            pendingIpBanUser.markForIpBan(BAN_REASON);
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);

            entityManager.persist(pendingIpBanUser);
            entityManager.persist(regularUser);
            entityManager.flush();

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
        @DisplayName("Should return empty page when no pending IP ban users exist")
        void findByIsPendingIpBanTrue_ShouldReturnEmptyPage_WhenNoPendingUsersExist() {
            // Given
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(regularUser);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByIsPendingIpBanTrue(pageable);

            // Then
            assertThat(page.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count pending IP ban users correctly")
        void countByIsPendingIpBanTrue_ShouldReturnCorrectCount_WhenPendingUsersExist() {
            // Given
            User pendingUser1 = new User(PENDING_IP_BAN_USERNAME + "1", TEST_PASSWORD);
            pendingUser1.markForIpBan(BAN_REASON);
            User pendingUser2 = new User(PENDING_IP_BAN_USERNAME + "2", TEST_PASSWORD);
            pendingUser2.markForIpBan(BAN_REASON);
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);

            entityManager.persist(pendingUser1);
            entityManager.persist(pendingUser2);
            entityManager.persist(regularUser);
            entityManager.flush();

            // When
            Long count = userRepository.countByIsPendingIpBanTrue();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero count when no pending IP ban users exist")
        void countByIsPendingIpBanTrue_ShouldReturnZero_WhenNoPendingUsersExist() {
            // Given
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
            entityManager.persistAndFlush(regularUser);

            // When
            Long count = userRepository.countByIsPendingIpBanTrue();

            // Then
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("User Deletion Operations")
    class UserDeletionTests {

        @Test
        @DisplayName("Should delete regular user when user is not admin")
        void deleteByIdAndRoleIsNot_ShouldDeleteRegularUser_WhenUserIsNotAdmin() {
            // Given
            User regularUser = new User(TEST_USERNAME, TEST_PASSWORD);
            regularUser.setRole(UserRoles.REGULAR);
            entityManager.persistAndFlush(regularUser);
            Long userId = regularUser.getId();

            // Verify user exists
            assertThat(userRepository.findById(userId)).isPresent();

            // When
            userRepository.deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);
            entityManager.flush();

            // Then
            assertThat(userRepository.findById(userId)).isEmpty();
        }

        @Test
        @DisplayName("Should not delete admin user when user is admin")
        void deleteByIdAndRoleIsNot_ShouldNotDeleteAdminUser_WhenUserIsAdmin() {
            // Given
            User adminUser = new User("adminUser", TEST_PASSWORD);
            adminUser.setRole(UserRoles.ADMIN);
            entityManager.persistAndFlush(adminUser);
            Long userId = adminUser.getId();

            // Verify user exists
            assertThat(userRepository.findById(userId)).isPresent();

            // When
            userRepository.deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);
            entityManager.flush();

            // Then
            assertThat(userRepository.findById(userId)).isPresent();
        }

        @Test
        @DisplayName("Should handle deletion of non-existent user gracefully")
        void deleteByIdAndRoleIsNot_ShouldHandleNonExistentId() {
            // Given
            Long nonExistentId = 99999L;

            // When & Then - Should not throw any exception
            userRepository.deleteByIdAndRoleIsNot(nonExistentId, UserRoles.ADMIN);
            entityManager.flush();
        }

        @ParameterizedTest
        @ValueSource(strings = {"REGULAR", "ADMIN"})
        @DisplayName("Should handle different user roles in deletion")
        void deleteByIdAndRoleIsNot_ShouldHandleDifferentRoles(String roleString) {
            // Given
            UserRoles role = UserRoles.valueOf(roleString);
            User user = new User("user_" + roleString.toLowerCase(), TEST_PASSWORD);
            user.setRole(role);
            entityManager.persistAndFlush(user);
            Long userId = user.getId();

            // When
            userRepository.deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);
            entityManager.flush();

            // Then
            if (role == UserRoles.ADMIN) {
                assertThat(userRepository.findById(userId)).isPresent();
            } else {
                assertThat(userRepository.findById(userId)).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Search Operations")
    class SearchOperationsTests {

        @Test
        @DisplayName("Should search users by username keyword")
        void searchByKeyword_ShouldReturnUsers_WhenUsernameMatches() {
            // Given
            User user1 = new User("testUser1", TEST_PASSWORD);
            user1.setNickname("Test User 1");
            User user2 = new User("anotherUser", TEST_PASSWORD);
            user2.setNickname("Another User");
            User user3 = new User("differentUser", TEST_PASSWORD);
            user3.setNickname("Different User");
            entityManager.persist(user1);
            entityManager.persist(user2);
            entityManager.persist(user3);
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.searchByKeyword("test", pageable);

            // Then
            assertThat(result.getContent())
                    .hasSize(1)
                    .first()
                    .satisfies(user -> {
                        assertThat(user.getUsername()).isEqualTo("testUser1");
                        assertThat(user.getNickname()).isEqualTo("Test User 1");
                    });
        }

        @Test
        @DisplayName("Should search users by nickname keyword")
        void searchByKeyword_ShouldReturnUsers_WhenNicknameMatches() {
            // Given
            User user1 = new User("user1", TEST_PASSWORD);
            user1.setNickname("Test User 1");
            User user2 = new User("user2", TEST_PASSWORD);
            user2.setNickname("Another User");
            entityManager.persist(user1);
            entityManager.persist(user2);
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.searchByKeyword("Test", pageable);

            // Then
            assertThat(result.getContent())
                    .hasSize(1)
                    .first()
                    .satisfies(user -> {
                        assertThat(user.getUsername()).isEqualTo("user1");
                        assertThat(user.getNickname()).isEqualTo("Test User 1");
                    });
        }

        @Test
        @DisplayName("Should be case insensitive for keyword search")
        void searchByKeyword_ShouldBeCaseInsensitive() {
            // Given
            User user = new User("TestUser", TEST_PASSWORD);
            user.setNickname("Test User");
            entityManager.persistAndFlush(user);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result1 = userRepository.searchByKeyword("test", pageable);
            Page<User> result2 = userRepository.searchByKeyword("TEST", pageable);
            Page<User> result3 = userRepository.searchByKeyword("Test", pageable);

            // Then
            assertThat(result1.getContent()).hasSize(1);
            assertThat(result2.getContent()).hasSize(1);
            assertThat(result3.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when no users match keyword")
        void searchByKeyword_ShouldReturnEmpty_WhenNoUsersMatch() {
            // Given
            User user = new User("user", TEST_PASSWORD);
            user.setNickname("User");
            entityManager.persistAndFlush(user);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.searchByKeyword("nomatch", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count users matching keyword")
        void countByKeyword_ShouldReturnCorrectCount_WhenUsersMatch() {
            // Given
            User user1 = new User("testUser1", TEST_PASSWORD);
            user1.setNickname("Test User 1");
            User user2 = new User("testUser2", TEST_PASSWORD);
            user2.setNickname("Test User 2");
            User user3 = new User("otherUser", TEST_PASSWORD);
            user3.setNickname("Other User");
            entityManager.persist(user1);
            entityManager.persist(user2);
            entityManager.persist(user3);
            entityManager.flush();

            // When
            Long count = userRepository.countByKeyword("test");

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero count when no users match keyword")
        void countByKeyword_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            User user = new User("user", TEST_PASSWORD);
            user.setNickname("User");
            entityManager.persistAndFlush(user);

            // When
            Long count = userRepository.countByKeyword("nomatch");

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle pagination for search results")
        void searchByKeyword_ShouldHandlePagination() {
            // Given
            for (int i = 1; i <= 5; i++) {
                User user = new User("testUser" + i, TEST_PASSWORD);
                user.setNickname("Test User " + i);
                entityManager.persist(user);
            }
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 3);

            // When
            Page<User> result = userRepository.searchByKeyword("test", pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Banned Users Search Operations")
    class BannedUsersSearchOperationsTests {

        @Test
        @DisplayName("Should search banned users by keyword")
        void searchBannedUsersByKeyword_ShouldReturnBannedUsers_WhenKeywordMatches() {
            // Given
            User bannedUser1 = new User("bannedTest1", TEST_PASSWORD);
            bannedUser1.setNickname("Banned Test User 1");
            bannedUser1.ban(BAN_REASON);

            User bannedUser2 = new User("bannedOther", TEST_PASSWORD);
            bannedUser2.setNickname("Banned Other User");
            bannedUser2.ban(BAN_REASON);

            User regularUser = new User("testUser", TEST_PASSWORD);
            regularUser.setNickname("Test User");

            entityManager.persist(bannedUser1);
            entityManager.persist(bannedUser2);
            entityManager.persist(regularUser);
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.searchBannedUsersByKeyword("test", pageable);

            // Then
            assertThat(result.getContent())
                    .hasSize(1)
                    .first()
                    .satisfies(user -> {
                        assertThat(user.getUsername()).isEqualTo("bannedTest1");
                        assertThat(user.getIsBanned()).isTrue();
                    });
        }

        @Test
        @DisplayName("Should return empty when no banned users match keyword")
        void searchBannedUsersByKeyword_ShouldReturnEmpty_WhenNoUsersMatch() {
            // Given
            User bannedUser = new User("bannedUser", TEST_PASSWORD);
            bannedUser.setNickname("Banned User");
            bannedUser.ban(BAN_REASON);
            entityManager.persistAndFlush(bannedUser);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.searchBannedUsersByKeyword("nomatch", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count banned users matching keyword")
        void countBannedUsersByKeyword_ShouldReturnCorrectCount_WhenUsersMatch() {
            // Given
            User bannedUser1 = new User("bannedTest1", TEST_PASSWORD);
            bannedUser1.setNickname("Banned Test User 1");
            bannedUser1.ban(BAN_REASON);

            User bannedUser2 = new User("bannedTest2", TEST_PASSWORD);
            bannedUser2.setNickname("Banned Test User 2");
            bannedUser2.ban(BAN_REASON);

            User regularUser = new User("testUser", TEST_PASSWORD);
            regularUser.setNickname("Test User");

            entityManager.persist(bannedUser1);
            entityManager.persist(bannedUser2);
            entityManager.persist(regularUser);
            entityManager.flush();

            // When
            Long count = userRepository.countBannedUsersByKeyword("test");

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero count when no banned users match keyword")
        void countBannedUsersByKeyword_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            User bannedUser = new User("bannedUser", TEST_PASSWORD);
            bannedUser.setNickname("Banned User");
            bannedUser.ban(BAN_REASON);
            entityManager.persistAndFlush(bannedUser);

            // When
            Long count = userRepository.countBannedUsersByKeyword("nomatch");

            // Then
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Pending Banned Users Search Operations")
    class PendingBannedUsersSearchOperationsTests {

        @Test
        @DisplayName("Should search pending banned users by keyword")
        void searchPendingBannedUsersByKeyword_ShouldReturnPendingUsers_WhenKeywordMatches() {
            // Given
            User pendingUser1 = new User("pendingTest1", TEST_PASSWORD);
            pendingUser1.setNickname("Pending Test User 1");
            pendingUser1.setIsPendingIpBan(true);

            User pendingUser2 = new User("pendingOther", TEST_PASSWORD);
            pendingUser2.setNickname("Pending Other User");
            pendingUser2.setIsPendingIpBan(true);

            User regularUser = new User("testUser", TEST_PASSWORD);
            regularUser.setNickname("Test User");

            entityManager.persist(pendingUser1);
            entityManager.persist(pendingUser2);
            entityManager.persist(regularUser);
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.searchPendingBannedUsersByKeyword("test", pageable);

            // Then
            assertThat(result.getContent())
                    .hasSize(1)
                    .first()
                    .satisfies(user -> {
                        assertThat(user.getUsername()).isEqualTo("pendingTest1");
                        assertThat(user.getIsPendingIpBan()).isTrue();
                    });
        }

        @Test
        @DisplayName("Should return empty when no pending banned users match keyword")
        void searchPendingBannedUsersByKeyword_ShouldReturnEmpty_WhenNoUsersMatch() {
            // Given
            User pendingUser = new User("pendingUser", TEST_PASSWORD);
            pendingUser.setNickname("Pending User");
            pendingUser.setIsPendingIpBan(true);
            entityManager.persistAndFlush(pendingUser);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.searchPendingBannedUsersByKeyword("nomatch", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count pending banned users matching keyword")
        void countPendingBannedUsersByKeyword_ShouldReturnCorrectCount_WhenUsersMatch() {
            // Given
            User pendingUser1 = new User("pendingTest1", TEST_PASSWORD);
            pendingUser1.setNickname("Pending Test User 1");
            pendingUser1.setIsPendingIpBan(true);

            User pendingUser2 = new User("pendingTest2", TEST_PASSWORD);
            pendingUser2.setNickname("Pending Test User 2");
            pendingUser2.setIsPendingIpBan(true);

            User regularUser = new User("testUser", TEST_PASSWORD);
            regularUser.setNickname("Test User");

            entityManager.persist(pendingUser1);
            entityManager.persist(pendingUser2);
            entityManager.persist(regularUser);
            entityManager.flush();

            // When
            Long count = userRepository.countPendingBannedUsersByKeyword("test");

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero count when no pending banned users match keyword")
        void countPendingBannedUsersByKeyword_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            User pendingUser = new User("pendingUser", TEST_PASSWORD);
            pendingUser.setNickname("Pending User");
            pendingUser.setIsPendingIpBan(true);
            entityManager.persistAndFlush(pendingUser);

            // When
            Long count = userRepository.countPendingBannedUsersByKeyword("nomatch");

            // Then
            assertThat(count).isEqualTo(0);
        }
    }
}
