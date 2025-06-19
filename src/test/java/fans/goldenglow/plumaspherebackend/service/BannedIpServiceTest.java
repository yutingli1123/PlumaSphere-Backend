package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import fans.goldenglow.plumaspherebackend.repository.BannedIpRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BannedIpServiceTest {

    private static final String TEST_IP_ADDRESS = "192.168.1.100";
    private static final String TEST_REASON = "Suspicious activity";
    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusDays(1);
    @InjectMocks
    private BannedIpService bannedIpService;
    @Mock
    private BannedIpRepository bannedIpRepository;
    private BannedIp testBannedIp;

    @BeforeEach
    void setUp() {
        testBannedIp = new BannedIp(TEST_IP_ADDRESS, TEST_REASON, FUTURE_DATE);
        testBannedIp.setId(1L);
    }

    @Nested
    @DisplayName("Ban check logic")
    class BanCheckTests {
        @Test
        @DisplayName("Should return true if IP is banned and not expired")
        void isIpBanned_ShouldReturnTrue_WhenIpIsBannedAndNotExpired() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(true);

            // When
            boolean result = bannedIpService.isIpBanned(TEST_IP_ADDRESS);

            // Then
            assertThat(result).isTrue();
            verify(bannedIpRepository).existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should return false if IP is not banned")
        void isIpBanned_ShouldReturnFalse_WhenIpIsNotBanned() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(false);

            // When
            boolean result = bannedIpService.isIpBanned(TEST_IP_ADDRESS);

            // Then
            assertThat(result).isFalse();
            verify(bannedIpRepository).existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Ban creation logic")
    class BanTests {
        @Test
        @DisplayName("Should create permanent ban if IP is not already banned")
        void banIp_ShouldCreatePermanentBan_WhenIpIsNotAlreadyBanned() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(false);
            when(bannedIpRepository.save(any(BannedIp.class))).thenReturn(testBannedIp);

            // When
            bannedIpService.banIp(TEST_IP_ADDRESS, TEST_REASON);

            // Then
            verify(bannedIpRepository).save(any(BannedIp.class));
            verify(bannedIpRepository).existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should not create ban if IP is already banned")
        void banIp_ShouldNotCreateBan_WhenIpIsAlreadyBanned() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(true);

            // When
            bannedIpService.banIp(TEST_IP_ADDRESS, TEST_REASON);

            // Then
            verify(bannedIpRepository, never()).save(any(BannedIp.class));
            verify(bannedIpRepository).existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should create temporary ban if IP is not already banned")
        void banIpTemporary_ShouldCreateTemporaryBan_WhenIpIsNotAlreadyBanned() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(false);
            when(bannedIpRepository.save(any(BannedIp.class))).thenReturn(testBannedIp);

            // When
            bannedIpService.banIpTemporary(TEST_IP_ADDRESS, TEST_REASON, FUTURE_DATE);

            // Then
            verify(bannedIpRepository).save(any(BannedIp.class));
            verify(bannedIpRepository).existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should not create temporary ban if IP is already banned")
        void banIpTemporary_ShouldNotCreateBan_WhenIpIsAlreadyBanned() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(true);

            // When
            bannedIpService.banIpTemporary(TEST_IP_ADDRESS, TEST_REASON, FUTURE_DATE);

            // Then
            verify(bannedIpRepository, never()).save(any(BannedIp.class));
            verify(bannedIpRepository).existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Unban logic")
    class UnbanTests {
        @Test
        @DisplayName("Should delete ban if IP exists")
        void unbanIp_ShouldDeleteBan_WhenIpExists() {
            // Given
            when(bannedIpRepository.existsByIpAddress(TEST_IP_ADDRESS)).thenReturn(true);

            // When
            bannedIpService.unbanIp(TEST_IP_ADDRESS);

            // Then
            verify(bannedIpRepository).existsByIpAddress(TEST_IP_ADDRESS);
            verify(bannedIpRepository).deleteByIpAddress(TEST_IP_ADDRESS);
        }

        @Test
        @DisplayName("Should not delete ban if IP does not exist")
        void unbanIp_ShouldNotDeleteBan_WhenIpDoesNotExist() {
            // Given
            when(bannedIpRepository.existsByIpAddress(TEST_IP_ADDRESS)).thenReturn(false);

            // When
            bannedIpService.unbanIp(TEST_IP_ADDRESS);

            // Then
            verify(bannedIpRepository).existsByIpAddress(TEST_IP_ADDRESS);
            verify(bannedIpRepository, never()).deleteByIpAddress(any());
        }
    }

    @Nested
    @DisplayName("Ban query logic")
    class QueryTests {
        @Test
        @DisplayName("Should return page of banned IPs")
        void getAllBans_ShouldReturnPageOfBannedIps() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<BannedIp> expectedPage = new PageImpl<>(Collections.singletonList(testBannedIp));
            when(bannedIpRepository.findAll(pageRequest)).thenReturn(expectedPage);

            // When
            Page<BannedIp> result = bannedIpService.getAllBans(pageRequest);

            // Then
            assertThat(result).isEqualTo(expectedPage);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst()).isEqualTo(testBannedIp);
            verify(bannedIpRepository).findAll(pageRequest);
        }

        @Test
        @DisplayName("Should return empty page if no bans exist")
        void getAllBans_ShouldReturnEmptyPage_WhenNoBansExist() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<BannedIp> emptyPage = new PageImpl<>(List.of());
            when(bannedIpRepository.findAll(pageRequest)).thenReturn(emptyPage);

            // When
            Page<BannedIp> result = bannedIpService.getAllBans(pageRequest);

            // Then
            assertThat(result).isEqualTo(emptyPage);
            assertThat(result.getContent()).isEmpty();
            verify(bannedIpRepository).findAll(pageRequest);
        }

        @Test
        @DisplayName("Should return correct count of banned IPs")
        void countBannedIps_ShouldReturnCorrectCount() {
            // Given
            long expectedCount = 5L;
            when(bannedIpRepository.count()).thenReturn(expectedCount);

            // When
            long result = bannedIpService.countBannedIps();

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(bannedIpRepository).count();
        }

        @Test
        @DisplayName("Should return zero if no bans exist")
        void countBannedIps_ShouldReturnZero_WhenNoBansExist() {
            // Given
            when(bannedIpRepository.count()).thenReturn(0L);

            // When
            long result = bannedIpService.countBannedIps();

            // Then
            assertThat(result).isEqualTo(0L);
            verify(bannedIpRepository).count();
        }
    }

    @Nested
    @DisplayName("Cleanup expired bans logic")
    class CleanupTests {
        @Test
        @DisplayName("Should delete expired bans")
        void cleanupExpiredBans_ShouldDeleteExpiredBans() {
            // When
            bannedIpService.cleanupExpiredBans();

            // Then
            verify(bannedIpRepository).deleteAllByExpiresAtBefore(any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("Ban property logic")
    class BanPropertyTests {
        @Test
        @DisplayName("Should create banned IP with correct properties for permanent ban")
        void banIp_ShouldCreateBannedIpWithCorrectProperties() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(false);

            // When
            bannedIpService.banIp(TEST_IP_ADDRESS, TEST_REASON);

            // Then
            verify(bannedIpRepository).save(argThat(bannedIp ->
                    bannedIp.getIpAddress().equals(TEST_IP_ADDRESS) &&
                            bannedIp.getReason().equals(TEST_REASON) &&
                            bannedIp.getExpiresAt() == null // Permanent ban
            ));
        }

        @Test
        @DisplayName("Should create banned IP with correct properties for temporary ban")
        void banIpTemporary_ShouldCreateBannedIpWithCorrectProperties() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(false);

            // When
            bannedIpService.banIpTemporary(TEST_IP_ADDRESS, TEST_REASON, FUTURE_DATE);

            // Then
            verify(bannedIpRepository).save(argThat(bannedIp ->
                    bannedIp.getIpAddress().equals(TEST_IP_ADDRESS) &&
                            bannedIp.getReason().equals(TEST_REASON) &&
                            bannedIp.getExpiresAt().equals(FUTURE_DATE)
            ));
        }

        @Test
        @DisplayName("Should handle null reason for permanent ban")
        void banIp_ShouldHandleNullReason() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(false);

            // When
            bannedIpService.banIp(TEST_IP_ADDRESS, null);

            // Then
            verify(bannedIpRepository).save(argThat(bannedIp ->
                    bannedIp.getIpAddress().equals(TEST_IP_ADDRESS) &&
                            bannedIp.getReason() == null &&
                            bannedIp.getExpiresAt() == null
            ));
        }

        @Test
        @DisplayName("Should handle null reason for temporary ban")
        void banIpTemporary_ShouldHandleNullReason() {
            // Given
            when(bannedIpRepository.existsByIpAddressAndExpiresAtAfter(eq(TEST_IP_ADDRESS), any(LocalDateTime.class)))
                    .thenReturn(false);

            // When
            bannedIpService.banIpTemporary(TEST_IP_ADDRESS, null, FUTURE_DATE);

            // Then
            verify(bannedIpRepository).save(argThat(bannedIp ->
                    bannedIp.getIpAddress().equals(TEST_IP_ADDRESS) &&
                            bannedIp.getReason() == null &&
                            bannedIp.getExpiresAt().equals(FUTURE_DATE)
            ));
        }
    }
}
