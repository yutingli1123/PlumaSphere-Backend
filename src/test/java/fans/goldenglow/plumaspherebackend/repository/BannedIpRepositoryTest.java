package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BannedIpRepositoryTest {
    private final BannedIpRepository bannedIpRepository;

    private final String TEST_IP_ADDRESS = "testIpAddress";
    private final String TEST_REASON = "testReason";
    private final LocalDateTime TEST_EXPIRES_AT = LocalDateTime.now().plusDays(1);

    @Autowired
    public BannedIpRepositoryTest(BannedIpRepository bannedIpRepository) {
        this.bannedIpRepository = bannedIpRepository;
    }

    @BeforeEach
    public void setupBannedIp() {
        BannedIp bannedIp = new BannedIp(TEST_IP_ADDRESS, TEST_REASON, TEST_EXPIRES_AT);
        bannedIpRepository.save(bannedIp);
    }

    @Test
    public void testFindByIpAddressAndExpiresAtAfter() {
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
    public void testDeleteAllByExpiresAtBefore() {
        LocalDateTime futureDate = TEST_EXPIRES_AT.plusDays(1);
        bannedIpRepository.deleteAllByExpiresAtBefore(futureDate);

        List<BannedIp> bannedIps = bannedIpRepository.findAll();
        assertThat(bannedIps).isEmpty();
    }

    @Test
    public void testDeleteByIpAddress() {
        bannedIpRepository.deleteByIpAddress(TEST_IP_ADDRESS);

        Optional<BannedIp> foundBannedIp = bannedIpRepository.findByIpAddressAndExpiresAtAfter(TEST_IP_ADDRESS, LocalDateTime.now());
        assertThat(foundBannedIp).isNotPresent();
    }

    @Test
    public void testExistsByIpAddress() {
        boolean exists = bannedIpRepository.existsByIpAddress(TEST_IP_ADDRESS);
        assertThat(exists).isTrue();

        boolean notExists = bannedIpRepository.existsByIpAddress("nonExistentIp");
        assertThat(notExists).isFalse();
    }

    @Test
    public void testExistsByIpAddressAndExpiresAtAfter() {
        boolean exists = bannedIpRepository.existsByIpAddressAndExpiresAtAfter(TEST_IP_ADDRESS, LocalDateTime.now());
        assertThat(exists).isTrue();

        boolean notExists = bannedIpRepository.existsByIpAddressAndExpiresAtAfter("nonExistentIp", LocalDateTime.now());
        assertThat(notExists).isFalse();
    }
}
