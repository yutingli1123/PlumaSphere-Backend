package fans.goldenglow.plumaspherebackend.repository;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    public void testFindByUsername() {
        String username = "testUser";
        User user = new User(username, "testPassword");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername(username);
        assertThat(foundUser)
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo(username);
    }

    @Test
    public void testExistsByUsername() {
        String username = "testUser";
        User user = new User(username, "testPassword");
        userRepository.save(user);
        boolean exists = userRepository.existsByUsername(username);
        assertThat(exists).isTrue();
    }

    @Test
    public void testFindByIsBannedTrue() {
        User bannedUser = new User("bannedUser", "password");
        bannedUser.ban("reason");
        userRepository.save(bannedUser);

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var page = userRepository.findByIsBannedTrue(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getUsername()).isEqualTo("bannedUser");
        assertThat(page.getContent().getFirst().getBanReason()).isEqualTo("reason");
    }

    @Test
    public void testFindByIsPendingIpBanTrue() {
        User pendingIpBanUser = new User("pendingIpBanUser", "password");
        pendingIpBanUser.markForIpBan("reason");
        userRepository.save(pendingIpBanUser);

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var page = userRepository.findByIsPendingIpBanTrue(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getUsername()).isEqualTo("pendingIpBanUser");
        assertThat(page.getContent().getFirst().getIpBanReason()).isEqualTo("reason");
    }

    @Test
    public void testCountByIsBannedTrue() {
        User bannedUser = new User("bannedUser", "password");
        bannedUser.ban("reason");
        userRepository.save(bannedUser);

        Long count = userRepository.countByIsBannedTrue();
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testCountByIsPendingIpBanTrue() {
        User pendingIpBanUser = new User("pendingIpBanUser", "password");
        pendingIpBanUser.markForIpBan("reason");
        userRepository.save(pendingIpBanUser);

        Long count = userRepository.countByIsPendingIpBanTrue();
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testFindUserByBanExpiresAtBefore() {
        User user = new User("testUser", "password");
        user.ban("reason");
        user.setBanExpiresAt(java.time.LocalDateTime.now().minusDays(1));
        userRepository.save(user);

        var users = userRepository.findUserByBanExpiresAtBefore(java.time.LocalDateTime.now());
        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getUsername()).isEqualTo("testUser");
        assertThat(users.getFirst().getBanReason()).isEqualTo("reason");
    }

    @Test
    public void testDeleteByIdAndRoleIsNot() {
        User user = new User("testUser", "password");
        user.setRole(UserRoles.REGULAR);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        Optional<User> foundUser = userRepository.findById(userId);
        assertThat(foundUser)
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("testUser");

        userRepository.deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }
}
