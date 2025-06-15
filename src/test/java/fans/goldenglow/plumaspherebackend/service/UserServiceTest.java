package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_NICKNAME = "testNickname";
    private static final Long TEST_USER_ID = 1L;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME);
        testUser.setId(TEST_USER_ID);
        testUser.setRole(UserRoles.REGULAR);

        adminUser = new User("admin", "adminPass", "Administrator");
        adminUser.setId(2L);
        adminUser.setRole(UserRoles.ADMIN);
    }

    @Test
    void findAll_ShouldReturnUserList_WhenUsersExist() {
        // Given
        User user1 = new User("user1", "password1");
        User user2 = new User("user2", "password2");
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<User> userList = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(userList, pageRequest, userList.size());

        when(userRepository.findAll(pageRequest)).thenReturn(userPage);

        // When
        List<User> result = userService.findAll(pageRequest);

        // Then
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(user1, user2);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
        verify(userRepository).findAll(pageRequest);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(userRepository.findAll(pageRequest)).thenReturn(emptyPage);

        // When
        List<User> result = userService.findAll(pageRequest);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findAll(pageRequest);
    }

    @Test
    void countAll_ShouldReturnCorrectCount_WhenUsersExist() {
        // Given
        long expectedCount = 5L;
        when(userRepository.count()).thenReturn(expectedCount);

        // When
        Long result = userService.countAll();

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(userRepository).count();
    }

    @Test
    void countAll_ShouldReturnZero_WhenNoUsersExist() {
        // Given
        when(userRepository.count()).thenReturn(0L);

        // When
        Long result = userService.countAll();

        // Then
        assertThat(result).isEqualTo(0L);
        verify(userRepository).count();
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(TEST_USER_ID);

        // Then
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(TEST_USER_ID);
                    assertThat(user.getUsername()).isEqualTo(TEST_USERNAME);
                    assertThat(user.getPassword()).isEqualTo(TEST_PASSWORD);
                    assertThat(user.getNickname()).isEqualTo(TEST_NICKNAME);
                    assertThat(user.getRole()).isEqualTo(UserRoles.REGULAR);
                });
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Given
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(nonExistentId);
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername(TEST_USERNAME);

        // Then
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(TEST_USERNAME);
                    assertThat(user.getPassword()).isEqualTo(TEST_PASSWORD);
                    assertThat(user.getNickname()).isEqualTo(TEST_NICKNAME);
                });
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Given
        String nonExistentUsername = "nonExistentUser";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername(nonExistentUsername);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(nonExistentUsername);
    }

    @Test
    void existByUsername_ShouldReturnTrue_WhenUserExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When
        boolean result = userService.existByUsername(TEST_USERNAME);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername(TEST_USERNAME);
    }

    @Test
    void existByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        // Given
        String nonExistentUsername = "nonExistentUser";
        when(userRepository.existsByUsername(nonExistentUsername)).thenReturn(false);

        // When
        boolean result = userService.existByUsername(nonExistentUsername);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByUsername(nonExistentUsername);
    }

    @Test
    void save_ShouldCallRepositorySave_WithCorrectUser() {
        // Given
        User newUser = new User("newUser", "newPassword");
        when(userRepository.save(newUser)).thenReturn(newUser);

        // When
        userService.save(newUser);

        // Then
        verify(userRepository).save(newUser);
    }

    @Test
    void save_ShouldHandleUserWithAllFields() {
        // Given
        User completeUser = new User(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME);
        completeUser.setRole(UserRoles.REGULAR);
        when(userRepository.save(completeUser)).thenReturn(completeUser);

        // When
        userService.save(completeUser);

        // Then
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(TEST_USERNAME) &&
                        user.getPassword().equals(TEST_PASSWORD) &&
                        user.getNickname().equals(TEST_NICKNAME) &&
                        user.getRole().equals(UserRoles.REGULAR)
        ));
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteByIdAndRoleIsNot_WithCorrectParameters() {
        // When
        userService.deleteById(TEST_USER_ID);

        // Then
        verify(userRepository).deleteByIdAndRoleIsNot(TEST_USER_ID, UserRoles.ADMIN);
    }

    @Test
    void findAll_ShouldHandleDifferentPageSizes() {
        // Given
        PageRequest smallPageRequest = PageRequest.of(0, 5);
        List<User> smallUserList = Collections.singletonList(testUser);
        Page<User> smallUserPage = new PageImpl<>(smallUserList, smallPageRequest, 1);

        when(userRepository.findAll(smallPageRequest)).thenReturn(smallUserPage);

        // When
        List<User> result = userService.findAll(smallPageRequest);

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).findAll(smallPageRequest);
    }

    @Test
    void findAll_ShouldHandlePageableCorrectly() {
        // Given
        Pageable customPageable = PageRequest.of(1, 20);
        List<User> userList = Arrays.asList(testUser, adminUser);
        Page<User> userPage = new PageImpl<>(userList, customPageable, 2);

        when(userRepository.findAll(customPageable)).thenReturn(userPage);

        // When
        List<User> result = userService.findAll(customPageable);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testUser, adminUser);
        verify(userRepository).findAll(customPageable);
    }

    @Test
    void existByUsername_ShouldHandleCaseSensitivity() {
        // Given
        String upperCaseUsername = TEST_USERNAME.toUpperCase();
        when(userRepository.existsByUsername(upperCaseUsername)).thenReturn(false);

        // When
        boolean result = userService.existByUsername(upperCaseUsername);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByUsername(upperCaseUsername);
    }

    @Test
    void findByUsername_ShouldHandleEmptyUsername() {
        // Given
        String emptyUsername = "";
        when(userRepository.findByUsername(emptyUsername)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername(emptyUsername);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(emptyUsername);
    }
}