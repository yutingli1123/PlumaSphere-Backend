package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("BannedIpRepository Tests")
class BannedIpRepositoryTest {
    private final BannedIpRepository bannedIpRepository;
    private final TestEntityManager entityManager;

    private final String TEST_IP_ADDRESS = "testIpAddress";
    private final String TEST_REASON = "testReason";
    private final LocalDateTime TEST_EXPIRES_AT = LocalDateTime.now().plusDays(1);

    @Autowired
    public BannedIpRepositoryTest(BannedIpRepository bannedIpRepository, TestEntityManager entityManager) {
        this.bannedIpRepository = bannedIpRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    public void setupBannedIp() {
        BannedIp bannedIp = new BannedIp(TEST_IP_ADDRESS, TEST_REASON, TEST_EXPIRES_AT);
        entityManager.persistAndFlush(bannedIp);
    }

    @Nested
    @DisplayName("IP Address Operations")
    class IpAddressOperationsTests {
        @Test
        @DisplayName("Should find by IP address and expiresAt after now")
        void findByIpAddressAndExpiresAtAfter_ShouldReturnBannedIp_WhenExists() {
            Optional<BannedIp> foundBannedIp = bannedIpRepository.findByIpAddressAndExpiresAtAfter(TEST_IP_ADDRESS, LocalDateTime.now());
            assertThat(foundBannedIp)
                    .isPresent()
                    .get()
                    .satisfies(bannedIp -> {
                        assertThat(bannedIp.getIpAddress()).isEqualTo(TEST_IP_ADDRESS);
                        assertThat(bannedIp.getReason()).isEqualTo(TEST_REASON);
                        assertThat(bannedIp.getExpiresAt()).isEqualTo(TEST_EXPIRES_AT);
                    });
        }

        @Test
        @DisplayName("Should delete all by expiresAt before given date")
        void deleteAllByExpiresAtBefore_ShouldDelete_WhenBeforeGivenDate() {
            LocalDateTime futureDate = TEST_EXPIRES_AT.plusDays(1);
            bannedIpRepository.deleteAllByExpiresAtBefore(futureDate);

            List<BannedIp> bannedIps = bannedIpRepository.findAll();
            assertThat(bannedIps).isEmpty();
        }

        @Test
        @DisplayName("Should delete by IP address")
        void deleteByIpAddress_ShouldDelete_WhenExists() {
            bannedIpRepository.deleteByIpAddress(TEST_IP_ADDRESS);

            Optional<BannedIp> foundBannedIp = bannedIpRepository.findByIpAddressAndExpiresAtAfter(TEST_IP_ADDRESS, LocalDateTime.now());
            assertThat(foundBannedIp).isNotPresent();
        }

        @Test
        @DisplayName("Should check existence by IP address")
        void existsByIpAddress_ShouldReturnCorrectResult() {
            boolean exists = bannedIpRepository.existsByIpAddress(TEST_IP_ADDRESS);
            assertThat(exists).isTrue();

            boolean notExists = bannedIpRepository.existsByIpAddress("nonExistentIp");
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("Should check existence by IP address and expiresAt after now")
        void existsByIpAddressAndExpiresAtAfter_ShouldReturnCorrectResult() {
            boolean exists = bannedIpRepository.existsByIpAddressAndExpiresAtAfter(TEST_IP_ADDRESS, LocalDateTime.now());
            assertThat(exists).isTrue();

            boolean notExists = bannedIpRepository.existsByIpAddressAndExpiresAtAfter("nonExistentIp", LocalDateTime.now());
            assertThat(notExists).isFalse();
        }
    }
}
