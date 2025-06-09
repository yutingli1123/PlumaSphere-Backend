package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void testFinaAll() {
        User expectedUser1 = new User("user1", "password1");
        User expectedUser2 = new User("user2", "password2");
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<User> userList = List.of(expectedUser1, expectedUser2);
        Page<User> userPage = new PageImpl<>(userList, pageRequest, userList.size());

        when(userRepository.findAll(pageRequest)).thenReturn(userPage);

        List<User> users = userService.findAll(pageRequest);

        assertThat(users)
                .isNotNull()
                .hasSize(2);
        assertThat(users)
                .extracting(User::getUsername)
                .containsExactly(expectedUser1.getUsername(), expectedUser2.getUsername());
        assertThat(users)
                .extracting(User::getPassword)
                .containsExactly(expectedUser1.getPassword(), expectedUser2.getPassword());
        verify(userRepository).findAll(pageRequest);
    }

    @Test
    void testCountAll() {
        long expectedCount = 5L;

        when(userRepository.count()).thenReturn(expectedCount);

        Long count = userService.countAll();

        assertThat(count).isEqualTo(expectedCount);
        verify(userRepository).count();
    }

    @Test
    void testFindById() {
        Long userId = 1L;
        User expectedUser = new User("username", "password");
        expectedUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        Optional<User> foundUser = userService.findById(userId);

        assertThat(foundUser)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(expectedUser.getId());
                    assertThat(user.getUsername()).isEqualTo(expectedUser.getUsername());
                    assertThat(user.getPassword()).isEqualTo(expectedUser.getPassword());
                });
        verify(userRepository).findById(userId);
    }

    @Test
    void testFindByUsername() {
        String username = "testUser";
        User expectedUser = new User(username, "password");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        Optional<User> foundUser = userService.findByUsername(username);

        assertThat(foundUser)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(expectedUser.getUsername());
                    assertThat(user.getPassword()).isEqualTo(expectedUser.getPassword());
                });
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testExistByUsername() {
        String username = "testUser";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean exists = userService.existByUsername(username);

        assertThat(exists).isTrue();
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void testSave() {
        User user = new User("newUser", "newPassword");

        userService.save(user);

        verify(userRepository).save(user);
    }

    @Test
    void testDeleteById() {
        Long userId = 1L;

        userService.deleteById(userId);

        verify(userRepository).deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);
    }

}