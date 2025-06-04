package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByIsBannedTrueOrderByBannedAtDesc(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isBanned = true AND u.banExpiresAt IS NOT NULL AND u.banExpiresAt <= :now")
    List<User> findExpiredBannedUsers(@Param("now") LocalDateTime now);
}
