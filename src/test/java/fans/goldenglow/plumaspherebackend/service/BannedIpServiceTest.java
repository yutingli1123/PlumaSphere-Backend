package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import fans.goldenglow.plumaspherebackend.repository.BannedIpRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
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

    @Test
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

    @Test
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
    void unbanIp_ShouldNotDeleteBan_WhenIpDoesNotExist() {
        // Given
        when(bannedIpRepository.existsByIpAddress(TEST_IP_ADDRESS)).thenReturn(false);

        // When
        bannedIpService.unbanIp(TEST_IP_ADDRESS);

        // Then
        verify(bannedIpRepository).existsByIpAddress(TEST_IP_ADDRESS);
        verify(bannedIpRepository, never()).deleteByIpAddress(any());
    }

    @Test
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
    void cleanupExpiredBans_ShouldDeleteExpiredBans() {
        // When
        bannedIpService.cleanupExpiredBans();

        // Then
        verify(bannedIpRepository).deleteAllByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
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

    @Test
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
