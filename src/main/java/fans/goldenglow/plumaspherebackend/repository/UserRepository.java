package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing users in the application.
 * Provides methods to find users by username, check existence, and manage banned users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> findByIsBannedTrue(Pageable pageable);

    Page<User> findByIsPendingIpBanTrue(Pageable pageable);

    Long countByIsBannedTrue();

    Long countByIsPendingIpBanTrue();

    List<User> findUserByBanExpiresAtBefore(LocalDateTime banExpiresAtBefore);

    void deleteByIdAndRoleIsNot(Long id, UserRoles role);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Long countByKeyword(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "u.isBanned = true")
    Page<User> searchBannedUsersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE " +
            "(LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "u.isBanned = true")
    Long countBannedUsersByKeyword(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "u.isPendingIpBan = true")
    Page<User> searchPendingBannedUsersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE " +
            "(LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "u.isPendingIpBan = true")
    Long countPendingBannedUsersByKeyword(@Param("keyword") String keyword);
}
