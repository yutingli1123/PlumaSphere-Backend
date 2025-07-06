package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserBanService Tests")
class UserBanServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserBanService userBanService;

    private User testUser;
    private User bannedUser;
    private User markedUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "password");
        testUser.setId(1L);

        bannedUser = new User("bannedUser", "password");
        bannedUser.setId(2L);
        bannedUser.setIsBanned(true);
        bannedUser.setBanReason("Test ban");

        markedUser = new User("markedUser", "password");
        markedUser.setId(3L);
        markedUser.setIsPendingIpBan(true);
    }

    @Nested
    @DisplayName("Ban Operations")
    class BanOperations {
        @Test
        @DisplayName("Should ban user permanently when user exists")
        void banUser_ShouldBanUserPermanently_WhenUserExists() {
            // Given
            String reason = "Violation of terms";
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userBanService.banUser(1L, reason);

            // Then
            verify(userRepository).findById(1L);
            verify(userRepository).save(argThat(user -> {
                assertThat(user.getId()).isEqualTo(1L);
                assertThat(user.getIsBanned()).isTrue();
                assertThat(user.getBanReason()).isEqualTo(reason);
                assertThat(user.getBanExpiresAt()).isNull(); // Permanent ban
                return true;
            }));
        }

        @Test
        @DisplayName("Should throw exception when user not found for permanent ban")
        void banUser_ShouldThrowException_WhenUserNotFound() {
            // Given
            Long nonExistentUserId = 999L;
            String reason = "Test reason";
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userBanService.banUser(nonExistentUserId, reason))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found: " + nonExistentUserId);

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should ban user temporarily when user exists")
        void banUserTemporary_ShouldBanUserTemporarily_WhenUserExists() {
            // Given
            String reason = "Temporary violation";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userBanService.banUserTemporary(1L, reason, expiresAt);

            // Then
            verify(userRepository).findById(1L);
            verify(userRepository).save(argThat(user -> {
                assertThat(user.getId()).isEqualTo(1L);
                assertThat(user.getIsBanned()).isTrue();
                assertThat(user.getBanReason()).isEqualTo(reason);
                assertThat(user.getBanExpiresAt()).isEqualTo(expiresAt);
                return true;
            }));
        }

        @Test
        @DisplayName("Should throw exception when user not found for temporary ban")
        void banUserTemporary_ShouldThrowException_WhenUserNotFound() {
            // Given
            Long nonExistentUserId = 999L;
            String reason = "Test reason";
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userBanService.banUserTemporary(nonExistentUserId, reason, expiresAt))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found: " + nonExistentUserId);

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle null reason for ban")
        void banUser_ShouldHandleNullReason() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userBanService.banUser(1L, null);

            // Then
            verify(userRepository).save(argThat(user -> {
                assertThat(user.getIsBanned()).isTrue();
                assertThat(user.getBanReason()).isNull();
                return true;
            }));
        }

        @Test
        @DisplayName("Should handle empty reason for ban")
        void banUser_ShouldHandleEmptyReason() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userBanService.banUser(1L, "");

            // Then
            verify(userRepository).save(argThat(user -> {
                assertThat(user.getIsBanned()).isTrue();
                assertThat(user.getBanReason()).isEqualTo("");
                return true;
            }));
        }

        @Test
        @DisplayName("Should handle past expiration date for temporary ban")
        void banUserTemporary_ShouldHandlePastExpirationDate() {
            // Given
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userBanService.banUserTemporary(1L, "Test", pastDate);

            // Then
            verify(userRepository).save(argThat(user -> {
                assertThat(user.getIsBanned()).isTrue();
                assertThat(user.getBanExpiresAt()).isEqualTo(pastDate);
                return true;
            }));
        }
    }

    @Nested
    @DisplayName("Unban Operations")
    class UnbanOperations {
        @Test
        @DisplayName("Should unban user when user exists")
        void unbanUser_ShouldUnbanUser_WhenUserExists() {
            // Given
            when(userRepository.findById(2L)).thenReturn(Optional.of(bannedUser));

            // When
            userBanService.unbanUser(2L);

            // Then
            verify(userRepository).findById(2L);
            verify(userRepository).save(argThat(user -> {
                assertThat(user.getId()).isEqualTo(2L);
                assertThat(user.getIsBanned()).isFalse();
                assertThat(user.getBanReason()).isNull();
                assertThat(user.getBanExpiresAt()).isNull();
                return true;
            }));
        }

        @Test
        @DisplayName("Should throw exception when user not found for unban")
        void unbanUser_ShouldThrowException_WhenUserNotFound() {
            // Given
            Long nonExistentUserId = 999L;
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userBanService.unbanUser(nonExistentUserId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found: " + nonExistentUserId);

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle already unbanned user")
        void unbanUser_ShouldHandleAlreadyUnbannedUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userBanService.unbanUser(1L);

            // Then
            verify(userRepository).save(argThat(user -> {
                assertThat(user.getIsBanned()).isFalse();
                return true;
            }));
        }
    }

    @Nested
    @DisplayName("Ban Status Checks")
    class BanStatusChecks {
        @Test
        @DisplayName("Should return true when user is banned")
        void isUserBanned_ShouldReturnTrue_WhenUserIsBanned() {
            // Given
            when(userRepository.findById(2L)).thenReturn(Optional.of(bannedUser));

            // When
            boolean result = userBanService.isUserBanned(2L);

            // Then
            assertThat(result).isTrue();
            verify(userRepository).findById(2L);
        }

        @Test
        @DisplayName("Should return false when user is not banned")
        void isUserBanned_ShouldReturnFalse_WhenUserIsNotBanned() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            boolean result = userBanService.isUserBanned(1L);

            // Then
            assertThat(result).isFalse();
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return false when user not found for ban check")
        void isUserBanned_ShouldReturnFalse_WhenUserNotFound() {
            // Given
            Long nonExistentUserId = 999L;
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            // When
            boolean result = userBanService.isUserBanned(nonExistentUserId);

            // Then
            assertThat(result).isFalse();
            verify(userRepository).findById(nonExistentUserId);
        }
    }

    @Nested
    @DisplayName("Banned Users Query")
    class BannedUsersQuery {
        @Test
        @DisplayName("Should return page of banned users")
        void getBannedUsers_ShouldReturnPageOfBannedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<User> bannedUsers = Collections.singletonList(bannedUser);
            Page<User> bannedUsersPage = new PageImpl<>(bannedUsers, pageable, 1);

            when(userRepository.findByIsBannedTrue(pageable)).thenReturn(bannedUsersPage);

            // When
            Page<User> result = userBanService.getBannedUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst()).isEqualTo(bannedUser);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(userRepository).findByIsBannedTrue(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no banned users")
        void getBannedUsers_ShouldReturnEmptyPage_WhenNoBannedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.findByIsBannedTrue(pageable)).thenReturn(emptyPage);

            // When
            Page<User> result = userBanService.getBannedUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(userRepository).findByIsBannedTrue(pageable);
        }

        @Test
        @DisplayName("Should return correct count of banned users")
        void countBannedUsers_ShouldReturnCorrectCount() {
            // Given
            when(userRepository.countByIsBannedTrue()).thenReturn(5L);

            // When
            Long result = userBanService.countBannedUsers();

            // Then
            assertThat(result).isEqualTo(5L);
            verify(userRepository).countByIsBannedTrue();
        }

        @Test
        @DisplayName("Should return zero when no banned users")
        void countBannedUsers_ShouldReturnZero_WhenNoBannedUsers() {
            // Given
            when(userRepository.countByIsBannedTrue()).thenReturn(0L);

            // When
            Long result = userBanService.countBannedUsers();

            // Then
            assertThat(result).isEqualTo(0L);
            verify(userRepository).countByIsBannedTrue();
        }
    }

    @Nested
    @DisplayName("Marked Users Query")
    class MarkedUsersQuery {
        @Test
        @DisplayName("Should return page of marked users")
        void getMarkedUsers_ShouldReturnPageOfMarkedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<User> markedUsers = Collections.singletonList(markedUser);
            Page<User> markedUsersPage = new PageImpl<>(markedUsers, pageable, 1);

            when(userRepository.findByIsPendingIpBanTrue(pageable)).thenReturn(markedUsersPage);

            // When
            Page<User> result = userBanService.getMarkedUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst()).isEqualTo(markedUser);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(userRepository).findByIsPendingIpBanTrue(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no marked users")
        void getMarkedUsers_ShouldReturnEmptyPage_WhenNoMarkedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.findByIsPendingIpBanTrue(pageable)).thenReturn(emptyPage);

            // When
            Page<User> result = userBanService.getMarkedUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(userRepository).findByIsPendingIpBanTrue(pageable);
        }

        @Test
        @DisplayName("Should return correct count of marked users")
        void countMarkedUsers_ShouldReturnCorrectCount() {
            // Given
            when(userRepository.countByIsPendingIpBanTrue()).thenReturn(3L);

            // When
            Long result = userBanService.countMarkedUsers();

            // Then
            assertThat(result).isEqualTo(3L);
            verify(userRepository).countByIsPendingIpBanTrue();
        }

        @Test
        @DisplayName("Should return zero when no marked users")
        void countMarkedUsers_ShouldReturnZero_WhenNoMarkedUsers() {
            // Given
            when(userRepository.countByIsPendingIpBanTrue()).thenReturn(0L);

            // When
            Long result = userBanService.countMarkedUsers();

            // Then
            assertThat(result).isEqualTo(0L);
            verify(userRepository).countByIsPendingIpBanTrue();
        }
    }

    @Nested
    @DisplayName("Cleanup Expired Bans")
    class CleanupExpiredBans {
        @Test
        @DisplayName("Should unban expired users")
        void cleanupExpiredBans_ShouldUnbanExpiredUsers() {
            // Given
            User expiredUser1 = new User("expired1", "password");
            expiredUser1.setId(4L);
            expiredUser1.setIsBanned(true);
            expiredUser1.setBanExpiresAt(LocalDateTime.now().minusHours(1));

            User expiredUser2 = new User("expired2", "password");
            expiredUser2.setId(5L);
            expiredUser2.setIsBanned(true);
            expiredUser2.setBanExpiresAt(LocalDateTime.now().minusDays(1));

            List<User> expiredUsers = Arrays.asList(expiredUser1, expiredUser2);

            when(userRepository.findUserByBanExpiresAtBefore(any(LocalDateTime.class)))
                    .thenReturn(expiredUsers);

            // When
            userBanService.cleanupExpiredBans();

            // Then
            verify(userRepository).findUserByBanExpiresAtBefore(any(LocalDateTime.class));
            verify(userRepository).saveAll(argThat(users -> {
                List<User> userList = (List<User>) users;
                assertThat(userList).hasSize(2);
                assertThat(userList.get(0).getIsBanned()).isFalse();
                assertThat(userList.get(1).getIsBanned()).isFalse();
                return true;
            }));
        }

        @Test
        @DisplayName("Should do nothing when no expired bans")
        void cleanupExpiredBans_ShouldDoNothing_WhenNoExpiredBans() {
            // Given
            when(userRepository.findUserByBanExpiresAtBefore(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            userBanService.cleanupExpiredBans();

            // Then
            verify(userRepository).findUserByBanExpiresAtBefore(any(LocalDateTime.class));
            verify(userRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Search Banned Users Operations")
    class SearchBannedUsersOperations {

        @Test
        @DisplayName("Should return banned users matching keyword")
        void searchBannedUsersByKeyword_ShouldReturnBannedUsers_WhenKeywordMatches() {
            // Given
            String keyword = "test";
            Pageable pageable = PageRequest.of(0, 10);
            User bannedUser1 = new User("testUser1", "password1");
            bannedUser1.setNickname("Test User 1");
            bannedUser1.setIsBanned(true);
            User bannedUser2 = new User("testUser2", "password2");
            bannedUser2.setNickname("Test User 2");
            bannedUser2.setIsBanned(true);
            List<User> bannedUsers = Arrays.asList(bannedUser1, bannedUser2);
            Page<User> bannedUsersPage = new PageImpl<>(bannedUsers, pageable, bannedUsers.size());

            when(userRepository.searchBannedUsersByKeyword(keyword, pageable)).thenReturn(bannedUsersPage);

            // When
            Page<User> result = userBanService.searchBannedUsersByKeyword(keyword, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent())
                    .hasSize(2)
                    .containsExactly(bannedUser1, bannedUser2);
            assertThat(result.getContent().get(0).getUsername()).isEqualTo("testUser1");
            assertThat(result.getContent().get(1).getUsername()).isEqualTo("testUser2");
            verify(userRepository).searchBannedUsersByKeyword(keyword, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no banned users match keyword")
        void searchBannedUsersByKeyword_ShouldReturnEmptyPage_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.searchBannedUsersByKeyword(keyword, pageable)).thenReturn(emptyPage);

            // When
            Page<User> result = userBanService.searchBannedUsersByKeyword(keyword, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            verify(userRepository).searchBannedUsersByKeyword(keyword, pageable);
        }

        @Test
        @DisplayName("Should handle different page sizes for banned users search")
        void searchBannedUsersByKeyword_ShouldHandleDifferentPageSizes() {
            // Given
            String keyword = "test";
            Pageable smallPageable = PageRequest.of(0, 5);
            List<User> smallUserList = Collections.singletonList(bannedUser);
            Page<User> smallUserPage = new PageImpl<>(smallUserList, smallPageable, 1);

            when(userRepository.searchBannedUsersByKeyword(keyword, smallPageable)).thenReturn(smallUserPage);

            // When
            Page<User> result = userBanService.searchBannedUsersByKeyword(keyword, smallPageable);

            // Then
            assertThat(result.getContent())
                    .hasSize(1)
                    .containsExactly(bannedUser);
            verify(userRepository).searchBannedUsersByKeyword(keyword, smallPageable);
        }

        @Test
        @DisplayName("Should return correct count of banned users matching keyword")
        void countBannedUsersByKeyword_ShouldReturnCorrectCount_WhenUsersMatch() {
            // Given
            String keyword = "test";
            Long expectedCount = 3L;

            when(userRepository.countBannedUsersByKeyword(keyword)).thenReturn(expectedCount);

            // When
            Long result = userBanService.countBannedUsersByKeyword(keyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countBannedUsersByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return zero count when no banned users match keyword")
        void countBannedUsersByKeyword_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";
            Long expectedCount = 0L;

            when(userRepository.countBannedUsersByKeyword(keyword)).thenReturn(expectedCount);

            // When
            Long result = userBanService.countBannedUsersByKeyword(keyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countBannedUsersByKeyword(keyword);
        }

        @Test
        @DisplayName("Should handle empty keyword for banned users search")
        void searchBannedUsersByKeyword_ShouldHandleEmptyKeyword() {
            // Given
            String emptyKeyword = "";
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.searchBannedUsersByKeyword(emptyKeyword, pageable)).thenReturn(emptyPage);

            // When
            Page<User> result = userBanService.searchBannedUsersByKeyword(emptyKeyword, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            verify(userRepository).searchBannedUsersByKeyword(emptyKeyword, pageable);
        }

        @Test
        @DisplayName("Should handle null keyword for banned users count")
        void countBannedUsersByKeyword_ShouldHandleNullKeyword() {
            // Given
            String nullKeyword = null;
            Long expectedCount = 0L;

            when(userRepository.countBannedUsersByKeyword(nullKeyword)).thenReturn(expectedCount);

            // When
            Long result = userBanService.countBannedUsersByKeyword(nullKeyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countBannedUsersByKeyword(nullKeyword);
        }
    }

    @Nested
    @DisplayName("Search Pending Banned Users Operations")
    class SearchPendingBannedUsersOperations {

        @Test
        @DisplayName("Should return pending banned users matching keyword")
        void searchPendingBannedUsersByKeyword_ShouldReturnPendingUsers_WhenKeywordMatches() {
            // Given
            String keyword = "test";
            Pageable pageable = PageRequest.of(0, 10);
            User pendingUser1 = new User("testPending1", "password1");
            pendingUser1.setNickname("Test Pending 1");
            pendingUser1.setIsPendingIpBan(true);
            User pendingUser2 = new User("testPending2", "password2");
            pendingUser2.setNickname("Test Pending 2");
            pendingUser2.setIsPendingIpBan(true);
            List<User> pendingUsers = Arrays.asList(pendingUser1, pendingUser2);
            Page<User> pendingUsersPage = new PageImpl<>(pendingUsers, pageable, pendingUsers.size());

            when(userRepository.searchPendingBannedUsersByKeyword(keyword, pageable)).thenReturn(pendingUsersPage);

            // When
            Page<User> result = userBanService.searchPendingBannedUsersByKeyword(keyword, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent())
                    .hasSize(2)
                    .containsExactly(pendingUser1, pendingUser2);
            assertThat(result.getContent().get(0).getUsername()).isEqualTo("testPending1");
            assertThat(result.getContent().get(1).getUsername()).isEqualTo("testPending2");
            verify(userRepository).searchPendingBannedUsersByKeyword(keyword, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no pending banned users match keyword")
        void searchPendingBannedUsersByKeyword_ShouldReturnEmptyPage_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.searchPendingBannedUsersByKeyword(keyword, pageable)).thenReturn(emptyPage);

            // When
            Page<User> result = userBanService.searchPendingBannedUsersByKeyword(keyword, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            verify(userRepository).searchPendingBannedUsersByKeyword(keyword, pageable);
        }

        @Test
        @DisplayName("Should handle different page sizes for pending banned users search")
        void searchPendingBannedUsersByKeyword_ShouldHandleDifferentPageSizes() {
            // Given
            String keyword = "test";
            Pageable smallPageable = PageRequest.of(0, 3);
            List<User> smallUserList = Collections.singletonList(markedUser);
            Page<User> smallUserPage = new PageImpl<>(smallUserList, smallPageable, 1);

            when(userRepository.searchPendingBannedUsersByKeyword(keyword, smallPageable)).thenReturn(smallUserPage);

            // When
            Page<User> result = userBanService.searchPendingBannedUsersByKeyword(keyword, smallPageable);

            // Then
            assertThat(result.getContent())
                    .hasSize(1)
                    .containsExactly(markedUser);
            verify(userRepository).searchPendingBannedUsersByKeyword(keyword, smallPageable);
        }

        @Test
        @DisplayName("Should return correct count of pending banned users matching keyword")
        void countPendingBannedUsersByKeyword_ShouldReturnCorrectCount_WhenUsersMatch() {
            // Given
            String keyword = "test";
            Long expectedCount = 2L;

            when(userRepository.countPendingBannedUsersByKeyword(keyword)).thenReturn(expectedCount);

            // When
            Long result = userBanService.countPendingBannedUsersByKeyword(keyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countPendingBannedUsersByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return zero count when no pending banned users match keyword")
        void countPendingBannedUsersByKeyword_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";
            Long expectedCount = 0L;

            when(userRepository.countPendingBannedUsersByKeyword(keyword)).thenReturn(expectedCount);

            // When
            Long result = userBanService.countPendingBannedUsersByKeyword(keyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countPendingBannedUsersByKeyword(keyword);
        }

        @Test
        @DisplayName("Should handle empty keyword for pending banned users search")
        void searchPendingBannedUsersByKeyword_ShouldHandleEmptyKeyword() {
            // Given
            String emptyKeyword = "";
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.searchPendingBannedUsersByKeyword(emptyKeyword, pageable)).thenReturn(emptyPage);

            // When
            Page<User> result = userBanService.searchPendingBannedUsersByKeyword(emptyKeyword, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            verify(userRepository).searchPendingBannedUsersByKeyword(emptyKeyword, pageable);
        }

        @Test
        @DisplayName("Should handle null keyword for pending banned users count")
        void countPendingBannedUsersByKeyword_ShouldHandleNullKeyword() {
            // Given
            String nullKeyword = null;
            Long expectedCount = 0L;

            when(userRepository.countPendingBannedUsersByKeyword(nullKeyword)).thenReturn(expectedCount);

            // When
            Long result = userBanService.countPendingBannedUsersByKeyword(nullKeyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countPendingBannedUsersByKeyword(nullKeyword);
        }

        @Test
        @DisplayName("Search and count methods should be consistent for pending users")
        void searchAndCountMethods_ShouldBeConsistent_ForPendingUsers() {
            // Given
            String keyword = "test";
            Pageable pageable = PageRequest.of(0, 10);
            List<User> pendingUsers = Collections.singletonList(markedUser);
            Page<User> pendingUsersPage = new PageImpl<>(pendingUsers, pageable, 1);
            Long count = 1L;

            when(userRepository.searchPendingBannedUsersByKeyword(keyword, pageable)).thenReturn(pendingUsersPage);
            when(userRepository.countPendingBannedUsersByKeyword(keyword)).thenReturn(count);

            // When
            Page<User> searchResult = userBanService.searchPendingBannedUsersByKeyword(keyword, pageable);
            Long countResult = userBanService.countPendingBannedUsersByKeyword(keyword);

            // Then
            assertThat(searchResult.getTotalElements()).isEqualTo(countResult);
            assertThat(countResult).isEqualTo(1L);
            verify(userRepository).searchPendingBannedUsersByKeyword(keyword, pageable);
            verify(userRepository).countPendingBannedUsersByKeyword(keyword);
        }
    }
}
