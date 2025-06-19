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
}
