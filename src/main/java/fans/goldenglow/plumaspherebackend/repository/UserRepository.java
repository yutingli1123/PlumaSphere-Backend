package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByIsBannedTrue(Pageable pageable);

    Long countByIsBannedTrue();

    List<User> findUserByBanExpiresAtBefore(LocalDateTime banExpiresAtBefore);
}
