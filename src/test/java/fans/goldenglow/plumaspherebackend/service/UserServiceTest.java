package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.UserRoles;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("UserService Tests")
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

    @Nested
    @DisplayName("Find Operations")
    class FindTests {
        @Test
        @DisplayName("Should return user list when users exist")
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
        @DisplayName("Should return empty list when no users exist")
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
        @DisplayName("Should return user when user exists by id")
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
        @DisplayName("Should return empty when user does not exist by id")
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
        @DisplayName("Should return user when user exists by username")
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
        @DisplayName("Should return empty when user does not exist by username")
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
        @DisplayName("Should handle empty username")
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

        @Test
        @DisplayName("Should handle different page sizes")
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
        @DisplayName("Should handle pageable correctly")
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
    }

    @Nested
    @DisplayName("Count Operations")
    class CountTests {
        @Test
        @DisplayName("Should return correct count when users exist")
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
        @DisplayName("Should return zero when no users exist")
        void countAll_ShouldReturnZero_WhenNoUsersExist() {
            // Given
            when(userRepository.count()).thenReturn(0L);

            // When
            Long result = userService.countAll();

            // Then
            assertThat(result).isEqualTo(0L);
            verify(userRepository).count();
        }
    }

    @Nested
    @DisplayName("Existence Operations")
    class ExistenceTests {
        @Test
        @DisplayName("Should return true when user exists by username")
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
        @DisplayName("Should return false when user does not exist by username")
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
        @DisplayName("Should handle case sensitivity for username existence")
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
    }

    @Nested
    @DisplayName("Save and Delete Operations")
    class SaveDeleteTests {
        @Test
        @DisplayName("Should call repository save with correct user")
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
        @DisplayName("Should handle user with all fields")
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
        @DisplayName("Should call repository deleteByIdAndRoleIsNot with correct parameters")
        void deleteById_ShouldCallRepositoryDeleteByIdAndRoleIsNot_WithCorrectParameters() {
            // Given
            Long userId = 123L;

            // When
            userService.deleteById(userId);

            // Then
            verify(userRepository).deleteByIdAndRoleIsNot(userId, UserRoles.ADMIN);
        }
    }

    @Nested
    @DisplayName("Search Operations")
    class SearchOperationsTests {

        @Test
        @DisplayName("Should return users matching keyword")
        void searchByKeyword_ShouldReturnUsers_WhenKeywordMatches() {
            // Given
            String keyword = "test";
            PageRequest pageRequest = PageRequest.of(0, 10);
            User user1 = new User("testUser1", "password1");
            user1.setNickname("Test User 1");
            User user2 = new User("testUser2", "password2");
            user2.setNickname("Test User 2");
            List<User> users = Arrays.asList(user1, user2);
            Page<User> userPage = new PageImpl<>(users, pageRequest, users.size());

            when(userRepository.searchByKeyword(keyword, pageRequest)).thenReturn(userPage);

            // When
            List<User> result = userService.searchByKeyword(keyword, pageRequest);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactly(user1, user2);
            assertThat(result.get(0).getUsername()).isEqualTo("testUser1");
            assertThat(result.get(1).getUsername()).isEqualTo("testUser2");
            verify(userRepository).searchByKeyword(keyword, pageRequest);
        }

        @Test
        @DisplayName("Should return empty list when no users match keyword")
        void searchByKeyword_ShouldReturnEmptyList_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

            when(userRepository.searchByKeyword(keyword, pageRequest)).thenReturn(emptyPage);

            // When
            List<User> result = userService.searchByKeyword(keyword, pageRequest);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).searchByKeyword(keyword, pageRequest);
        }

        @Test
        @DisplayName("Should handle different page sizes for search")
        void searchByKeyword_ShouldHandleDifferentPageSizes() {
            // Given
            String keyword = "test";
            PageRequest smallPageRequest = PageRequest.of(0, 5);
            List<User> smallUserList = Collections.singletonList(testUser);
            Page<User> smallUserPage = new PageImpl<>(smallUserList, smallPageRequest, 1);

            when(userRepository.searchByKeyword(keyword, smallPageRequest)).thenReturn(smallUserPage);

            // When
            List<User> result = userService.searchByKeyword(keyword, smallPageRequest);

            // Then
            assertThat(result)
                    .hasSize(1)
                    .containsExactly(testUser);
            verify(userRepository).searchByKeyword(keyword, smallPageRequest);
        }

        @Test
        @DisplayName("Should handle pagination for search results")
        void searchByKeyword_ShouldHandlePagination() {
            // Given
            String keyword = "test";
            PageRequest pageRequest = PageRequest.of(1, 2); // Second page, 2 items per page
            User user1 = new User("testUser3", "password3");
            user1.setNickname("Test User 3");
            User user2 = new User("testUser4", "password4");
            user2.setNickname("Test User 4");
            List<User> users = Arrays.asList(user1, user2);
            Page<User> userPage = new PageImpl<>(users, pageRequest, 10); // Total 10 items

            when(userRepository.searchByKeyword(keyword, pageRequest)).thenReturn(userPage);

            // When
            List<User> result = userService.searchByKeyword(keyword, pageRequest);

            // Then
            assertThat(result)
                    .hasSize(2)
                    .containsExactly(user1, user2);
            verify(userRepository).searchByKeyword(keyword, pageRequest);
        }

        @Test
        @DisplayName("Should handle empty keyword")
        void searchByKeyword_ShouldHandleEmptyKeyword() {
            // Given
            String emptyKeyword = "";
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

            when(userRepository.searchByKeyword(emptyKeyword, pageRequest)).thenReturn(emptyPage);

            // When
            List<User> result = userService.searchByKeyword(emptyKeyword, pageRequest);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).searchByKeyword(emptyKeyword, pageRequest);
        }

        @Test
        @DisplayName("Should handle null keyword gracefully")
        void searchByKeyword_ShouldHandleNullKeyword() {
            // Given
            String nullKeyword = null;
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

            when(userRepository.searchByKeyword(nullKeyword, pageRequest)).thenReturn(emptyPage);

            // When
            List<User> result = userService.searchByKeyword(nullKeyword, pageRequest);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).searchByKeyword(nullKeyword, pageRequest);
        }

        @Test
        @DisplayName("Should return correct count when users match keyword")
        void countByKeyword_ShouldReturnCorrectCount_WhenUsersMatch() {
            // Given
            String keyword = "test";
            Long expectedCount = 5L;

            when(userRepository.countByKeyword(keyword)).thenReturn(expectedCount);

            // When
            Long result = userService.countByKeyword(keyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return zero count when no users match keyword")
        void countByKeyword_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";
            Long expectedCount = 0L;

            when(userRepository.countByKeyword(keyword)).thenReturn(expectedCount);

            // When
            Long result = userService.countByKeyword(keyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countByKeyword(keyword);
        }

        @Test
        @DisplayName("Should handle empty keyword for count")
        void countByKeyword_ShouldHandleEmptyKeyword() {
            // Given
            String emptyKeyword = "";
            Long expectedCount = 0L;

            when(userRepository.countByKeyword(emptyKeyword)).thenReturn(expectedCount);

            // When
            Long result = userService.countByKeyword(emptyKeyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countByKeyword(emptyKeyword);
        }

        @Test
        @DisplayName("Should handle null keyword for count")
        void countByKeyword_ShouldHandleNullKeyword() {
            // Given
            String nullKeyword = null;
            Long expectedCount = 0L;

            when(userRepository.countByKeyword(nullKeyword)).thenReturn(expectedCount);

            // When
            Long result = userService.countByKeyword(nullKeyword);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countByKeyword(nullKeyword);
        }

        @Test
        @DisplayName("Search and count methods should be consistent")
        void searchAndCountMethods_ShouldBeConsistent() {
            // Given
            String keyword = "test";
            PageRequest pageRequest = PageRequest.of(0, 10);
            List<User> users = Arrays.asList(testUser, adminUser);
            Page<User> userPage = new PageImpl<>(users, pageRequest, 2);
            Long count = 2L;

            when(userRepository.searchByKeyword(keyword, pageRequest)).thenReturn(userPage);
            when(userRepository.countByKeyword(keyword)).thenReturn(count);

            // When
            List<User> searchResult = userService.searchByKeyword(keyword, pageRequest);
            Long countResult = userService.countByKeyword(keyword);

            // Then
            assertThat(searchResult).hasSize(2);
            assertThat(countResult).isEqualTo(2L);
            verify(userRepository).searchByKeyword(keyword, pageRequest);
            verify(userRepository).countByKeyword(keyword);
        }
    }
}