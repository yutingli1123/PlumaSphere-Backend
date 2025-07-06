package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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

    @Nested
    @DisplayName("Search Operations")
    class SearchOperationsTests {

        @Test
        @DisplayName("Should search banned IPs by keyword")
        void searchByKeyword_ShouldReturnBannedIps_WhenKeywordMatches() {
            // Given
            BannedIp ip1 = new BannedIp("192.168.1.1", "Test reason 1", LocalDateTime.now().plusDays(1));
            BannedIp ip2 = new BannedIp("10.0.0.1", "Test reason 2", LocalDateTime.now().plusDays(1));
            BannedIp ip3 = new BannedIp("172.16.0.1", "Different reason", LocalDateTime.now().plusDays(1));
            entityManager.persist(ip1);
            entityManager.persist(ip2);
            entityManager.persist(ip3);
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<BannedIp> result = bannedIpRepository.searchByKeyword("192.168", pageable);

            // Then
            assertThat(result.getContent())
                    .hasSize(1)
                    .first()
                    .satisfies(bannedIp -> {
                        assertThat(bannedIp.getIpAddress()).isEqualTo("192.168.1.1");
                        assertThat(bannedIp.getReason()).isEqualTo("Test reason 1");
                    });
        }

        @Test
        @DisplayName("Should be case insensitive for keyword search")
        void searchByKeyword_ShouldBeCaseInsensitive() {
            // Given
            BannedIp ip = new BannedIp("192.168.1.1", "Test reason", LocalDateTime.now().plusDays(1));
            entityManager.persistAndFlush(ip);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<BannedIp> result1 = bannedIpRepository.searchByKeyword("192.168", pageable);
            Page<BannedIp> result2 = bannedIpRepository.searchByKeyword("192.168", pageable);

            // Then
            assertThat(result1.getContent()).hasSize(1);
            assertThat(result2.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when no banned IPs match keyword")
        void searchByKeyword_ShouldReturnEmpty_WhenNoBannedIpsMatch() {
            // Given
            BannedIp ip = new BannedIp("192.168.1.1", "Test reason", LocalDateTime.now().plusDays(1));
            entityManager.persistAndFlush(ip);

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<BannedIp> result = bannedIpRepository.searchByKeyword("nomatch", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should count banned IPs matching keyword")
        void countByKeyword_ShouldReturnCorrectCount_WhenBannedIpsMatch() {
            // Given
            BannedIp ip1 = new BannedIp("192.168.1.1", "Test reason 1", LocalDateTime.now().plusDays(1));
            BannedIp ip2 = new BannedIp("192.168.1.2", "Test reason 2", LocalDateTime.now().plusDays(1));
            BannedIp ip3 = new BannedIp("10.0.0.1", "Different reason", LocalDateTime.now().plusDays(1));
            entityManager.persist(ip1);
            entityManager.persist(ip2);
            entityManager.persist(ip3);
            entityManager.flush();

            // When
            Long count = bannedIpRepository.countByKeyword("192.168");

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero count when no banned IPs match keyword")
        void countByKeyword_ShouldReturnZero_WhenNoBannedIpsMatch() {
            // Given
            BannedIp ip = new BannedIp("192.168.1.1", "Test reason", LocalDateTime.now().plusDays(1));
            entityManager.persistAndFlush(ip);

            // When
            Long count = bannedIpRepository.countByKeyword("nomatch");

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle pagination for search results")
        void searchByKeyword_ShouldHandlePagination() {
            // Given
            for (int i = 1; i <= 5; i++) {
                BannedIp ip = new BannedIp("192.168.1." + i, "Test reason " + i, LocalDateTime.now().plusDays(1));
                entityManager.persist(ip);
            }
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 3);

            // When
            Page<BannedIp> result = bannedIpRepository.searchByKeyword("192.168", pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should search by partial IP address")
        void searchByKeyword_ShouldReturnResults_WhenPartialIpMatches() {
            // Given
            BannedIp ip1 = new BannedIp("192.168.1.1", "Test reason 1", LocalDateTime.now().plusDays(1));
            BannedIp ip2 = new BannedIp("192.168.2.1", "Test reason 2", LocalDateTime.now().plusDays(1));
            BannedIp ip3 = new BannedIp("10.0.0.1", "Test reason 3", LocalDateTime.now().plusDays(1));
            entityManager.persist(ip1);
            entityManager.persist(ip2);
            entityManager.persist(ip3);
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<BannedIp> result1 = bannedIpRepository.searchByKeyword("192.168.1", pageable);
            Page<BannedIp> result2 = bannedIpRepository.searchByKeyword("10.0", pageable);

            // Then
            assertThat(result1.getContent()).hasSize(1);
            assertThat(result1.getContent().get(0).getIpAddress()).isEqualTo("192.168.1.1");

            assertThat(result2.getContent()).hasSize(1);
            assertThat(result2.getContent().get(0).getIpAddress()).isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("Count and search methods should be consistent")
        void countAndSearchMethods_ShouldBeConsistent() {
            // Given
            String keyword = "192.168";
            for (int i = 1; i <= 3; i++) {
                BannedIp ip = new BannedIp("192.168.1." + i, "Test reason " + i, LocalDateTime.now().plusDays(1));
                entityManager.persist(ip);
            }
            entityManager.flush();

            PageRequest pageable = PageRequest.of(0, 10);

            // When
            Page<BannedIp> searchResult = bannedIpRepository.searchByKeyword(keyword, pageable);
            Long count = bannedIpRepository.countByKeyword(keyword);

            // Then
            assertThat(searchResult.getTotalElements()).isEqualTo(count);
            assertThat(count).isEqualTo(3);
        }
    }
}
