package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for managing banned IP addresses.
 * Provides methods to find, delete, and check the existence of banned IPs.
 */
@Repository
public interface BannedIpRepository extends JpaRepository<BannedIp, Long> {
    Optional<BannedIp> findByIpAddressAndExpiresAtAfter(String ipAddress, LocalDateTime expiresAtAfter);

    void deleteAllByExpiresAtBefore(LocalDateTime expiresAtBefore);

    void deleteByIpAddress(String ipAddress);

    boolean existsByIpAddress(String ipAddress);

    boolean existsByIpAddressAndExpiresAtAfter(String ipAddress, LocalDateTime expiresAtAfter);

    @Query("SELECT b FROM BannedIp b WHERE " +
            "LOWER(b.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BannedIp> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(b) FROM BannedIp b WHERE " +
            "LOWER(b.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Long countByKeyword(@Param("keyword") String keyword);
}