package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BannedIpRepository extends JpaRepository<BannedIp, Long> {

    @Query("SELECT b FROM BannedIp b WHERE b.ipAddress = :ipAddress AND b.isActive = true AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    Optional<BannedIp> findActiveBanByIp(@Param("ipAddress") String ipAddress, @Param("now") LocalDateTime now);

    Page<BannedIp> findByIsActiveTrueOrderByBannedAtDesc(Pageable pageable);

    @Modifying
    @Query("UPDATE BannedIp b SET b.isActive = false WHERE b.expiresAt IS NOT NULL AND b.expiresAt <= :now")
    void deactivateExpiredBans(@Param("now") LocalDateTime now);
}