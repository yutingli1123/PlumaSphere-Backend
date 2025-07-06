package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.UserAdminDto;
import fans.goldenglow.plumaspherebackend.dto.UserDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import fans.goldenglow.plumaspherebackend.mapper.UserMapper;
import fans.goldenglow.plumaspherebackend.service.FileService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("UserController Tests")
class UserControllerTest {

    // Test constants
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_NICKNAME = "Test User";
    private static final String TEST_BIO = "Test Bio";
    private static final LocalDate TEST_DOB = LocalDate.of(2000, 1, 1);
    @InjectMocks
    private UserController userController;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private FileService fileService;
    // Test data
    private User testUser;
    private UserDto testUserDto;
    private UserAdminDto testUserAdminDto;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        setupTestData();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    private void setupTestData() {
        testUser = new User(TEST_USERNAME, "password", TEST_NICKNAME);
        testUser.setId(TEST_USER_ID);
        testUser.setBio(TEST_BIO);
        testUser.setDob(TEST_DOB);

        testUserDto = new UserDto();
        testUserDto.setId(TEST_USER_ID);
        testUserDto.setUsername(TEST_USERNAME);
        testUserDto.setNickname(TEST_NICKNAME);
        testUserDto.setBio(TEST_BIO);
        testUserDto.setDob(TEST_DOB);

        testUserAdminDto = new UserAdminDto();
        testUserAdminDto.setId(TEST_USER_ID);
        testUserAdminDto.setUsername(TEST_USERNAME);
        testUserAdminDto.setNickname(TEST_NICKNAME);
    }

    private JwtAuthenticationToken createMockJwtToken(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    @Nested
    @DisplayName("getAllUsers Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return list of users successfully")
        void getAllUsers_ShouldReturnUsersList_WhenUsersExist() {
            // Given
            List<User> users = Collections.singletonList(testUser);
            List<UserAdminDto> userAdminDtos = Collections.singletonList(testUserAdminDto);
            PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

            when(userService.findAll(pageRequest)).thenReturn(users);
            when(userMapper.toAdminDto(users)).thenReturn(userAdminDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = userController.getAllUsers(0);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull().hasSize(1);
            assertThat(response.getBody()).first().isEqualTo(testUserAdminDto);

            verify(userService).findAll(pageRequest);
            verify(userMapper).toAdminDto(users);
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
            // Given
            List<User> emptyUsers = Collections.emptyList();
            List<UserAdminDto> emptyAdminDtos = Collections.emptyList();
            PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

            when(userService.findAll(pageRequest)).thenReturn(emptyUsers);
            when(userMapper.toAdminDto(emptyUsers)).thenReturn(emptyAdminDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = userController.getAllUsers(0);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull().isEmpty();

            verify(userService).findAll(pageRequest);
            verify(userMapper).toAdminDto(emptyUsers);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10})
        @DisplayName("Should handle different page numbers correctly")
        void getAllUsers_ShouldHandleDifferentPages(int page) {
            // Given
            List<User> users = Collections.singletonList(testUser);
            List<UserAdminDto> userAdminDtos = Collections.singletonList(testUserAdminDto);
            PageRequest expectedPageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "id"));

            when(userService.findAll(expectedPageRequest)).thenReturn(users);
            when(userMapper.toAdminDto(users)).thenReturn(userAdminDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = userController.getAllUsers(page);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).findAll(expectedPageRequest);
        }
    }

    @Nested
    @DisplayName("getUserCount Tests")
    class GetUserCountTests {

        @Test
        @DisplayName("Should return correct user count")
        void getUserCount_ShouldReturnCorrectCount() {
            // Given
            long expectedCount = 100L;
            when(userService.countAll()).thenReturn(expectedCount);

            // When
            ResponseEntity<Long> response = userController.getUserCount();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedCount);
            verify(userService).countAll();
        }

        @Test
        @DisplayName("Should return zero when no users exist")
        void getUserCount_ShouldReturnZero_WhenNoUsersExist() {
            // Given
            when(userService.countAll()).thenReturn(0L);

            // When
            ResponseEntity<Long> response = userController.getUserCount();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(0L);
            verify(userService).countAll();
        }
    }

    @Nested
    @DisplayName("getUserPageCount Tests")
    class GetUserPageCountTests {

        @ParameterizedTest
        @ValueSource(longs = {0, 5, 10, 15, 100})
        @DisplayName("Should calculate correct page count for different user counts")
        void getUserPageCount_ShouldCalculateCorrectPageCount(long userCount) {
            // Given
            when(userService.countAll()).thenReturn(userCount);
            long expectedPageCount = (long) Math.ceil((double) userCount / 10);

            // When
            ResponseEntity<Long> response = userController.getUserPageCount();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedPageCount);
            verify(userService).countAll();
        }
    }

    @Nested
    @DisplayName("getSelf Tests")
    class GetSelfTests {

        @Test
        @DisplayName("Should return current user successfully")
        void getSelf_ShouldReturnUser_WhenTokenIsValid() {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userMapper.toDto(testUser)).thenReturn(testUserDto);

            // When
            ResponseEntity<UserDto> response = userController.getSelf(token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull().isEqualTo(testUserDto);
            verify(userService).findById(TEST_USER_ID);
            verify(userMapper).toDto(testUser);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user does not exist")
        void getSelf_ShouldReturnNotFound_WhenUserDoesNotExist() {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // When
            ResponseEntity<UserDto> response = userController.getSelf(token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(userService).findById(TEST_USER_ID);
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("updateUserInfo Tests")
    class UpdateUserInfoTests {

        @Test
        @DisplayName("Should update user info successfully")
        void updateUserInfo_ShouldUpdateSuccessfully_WhenValidData() {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            UserDto updateDto = new UserDto();
            updateDto.setNickname("Updated Nickname");
            updateDto.setBio("Updated Bio");
            updateDto.setDob(LocalDate.of(1990, 5, 15));

            // When
            ResponseEntity<Void> response = userController.updateUserInfo(updateDto, token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(testUser.getNickname()).isEqualTo("Updated Nickname");
            assertThat(testUser.getBio()).isEqualTo("Updated Bio");
            assertThat(testUser.getDob()).isEqualTo(LocalDate.of(1990, 5, 15));
            verify(userService).save(testUser);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user does not exist")
        void updateUserInfo_ShouldReturnNotFound_WhenUserDoesNotExist() {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            UserDto updateDto = new UserDto();
            updateDto.setNickname("Updated Nickname");

            // When
            ResponseEntity<Void> response = userController.updateUserInfo(updateDto, token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(userService, never()).save(any());
        }

        @Test
        @DisplayName("Should handle partial updates correctly")
        void updateUserInfo_ShouldHandlePartialUpdates_WhenSomeFieldsAreNull() {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            UserDto updateDto = new UserDto();
            updateDto.setNickname("Only Nickname Updated");
            // bio and dob are null - Controller will set them to null

            // When
            ResponseEntity<Void> response = userController.updateUserInfo(updateDto, token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(testUser.getNickname()).isEqualTo("Only Nickname Updated");
            // Controller sets null values directly, so original values are lost
            assertThat(testUser.getBio()).isNull();
            assertThat(testUser.getDob()).isNull();
            verify(userService).save(testUser);
        }
    }

    @Nested
    @DisplayName("updateUserAvatar Tests")
    class UpdateUserAvatarTests {

        @Test
        @DisplayName("Should update avatar successfully")
        void updateUserAvatar_ShouldUpdateSuccessfully_WhenValidFile() throws FileSaveException {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            MultipartFile file = mock(MultipartFile.class);
            String newAvatarUrl = "new-avatar-url";
            when(fileService.saveFile(file)).thenReturn(newAvatarUrl);

            // When
            ResponseEntity<Void> response = userController.updateUserAvatar(file, token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(testUser.getAvatarUrl()).isEqualTo(newAvatarUrl);
            verify(fileService).saveFile(file);
            verify(userService).save(testUser);
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user does not exist")
        void updateUserAvatar_ShouldReturnNotFound_WhenUserDoesNotExist() {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            MultipartFile file = mock(MultipartFile.class);

            // When
            ResponseEntity<Void> response = userController.updateUserAvatar(file, token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verifyNoInteractions(fileService);
            verify(userService, never()).save(any());
        }

        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR when file save fails")
        void updateUserAvatar_ShouldReturnServerError_WhenFileSaveFails() throws FileSaveException {
            // Given
            JwtAuthenticationToken token = createMockJwtToken(TEST_USER_ID.toString());
            when(userService.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            MultipartFile file = mock(MultipartFile.class);
            when(fileService.saveFile(file)).thenThrow(new FileSaveException("File save failed"));

            // When
            ResponseEntity<Void> response = userController.updateUserAvatar(file, token);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            verify(fileService).saveFile(file);
            verify(userService, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_ShouldDeleteSuccessfully_WhenUserExists() {
            // When
            ResponseEntity<Void> response = userController.deleteUser(TEST_USER_ID);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).deleteById(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should handle deletion of non-existent user")
        void deleteUser_ShouldReturnOk_WhenUserDoesNotExist() {
            // Given
            doNothing().when(userService).deleteById(TEST_USER_ID);

            // When
            ResponseEntity<Void> response = userController.deleteUser(TEST_USER_ID);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).deleteById(TEST_USER_ID);
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 100L, 999L, Long.MAX_VALUE})
        @DisplayName("Should handle different user IDs correctly")
        void deleteUser_ShouldHandleDifferentIds(Long userId) {
            // When
            ResponseEntity<Void> response = userController.deleteUser(userId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("searchUsers Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should return users matching keyword")
        void searchUsers_ShouldReturnUsers_WhenKeywordMatches() {
            // Given
            String keyword = "test";
            int page = 0;
            List<User> users = List.of(testUser);
            List<UserAdminDto> userAdminDtos = List.of(testUserAdminDto);
            PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname"));

            when(userService.searchByKeyword(keyword, pageRequest)).thenReturn(users);
            when(userMapper.toAdminDto(users)).thenReturn(userAdminDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = userController.searchUsers(keyword, page);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull().hasSize(1);
            assertThat(response.getBody()).first().isEqualTo(testUserAdminDto);

            verify(userService).searchByKeyword(keyword, pageRequest);
            verify(userMapper).toAdminDto(users);
        }

        @Test
        @DisplayName("Should return empty list when no users match keyword")
        void searchUsers_ShouldReturnEmptyList_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";
            int page = 0;
            List<User> emptyUsers = Collections.emptyList();
            List<UserAdminDto> emptyAdminDtos = Collections.emptyList();
            PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname"));

            when(userService.searchByKeyword(keyword, pageRequest)).thenReturn(emptyUsers);
            when(userMapper.toAdminDto(emptyUsers)).thenReturn(emptyAdminDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = userController.searchUsers(keyword, page);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull().isEmpty();

            verify(userService).searchByKeyword(keyword, pageRequest);
            verify(userMapper).toAdminDto(emptyUsers);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10})
        @DisplayName("Should handle different page numbers correctly")
        void searchUsers_ShouldHandleDifferentPages(int page) {
            // Given
            String keyword = "test";
            List<User> users = List.of(testUser);
            List<UserAdminDto> userAdminDtos = List.of(testUserAdminDto);
            PageRequest expectedPageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname"));

            when(userService.searchByKeyword(keyword, expectedPageRequest)).thenReturn(users);
            when(userMapper.toAdminDto(users)).thenReturn(userAdminDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = userController.searchUsers(keyword, page);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).searchByKeyword(keyword, expectedPageRequest);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "test", "TEST", "Test User"})
        @DisplayName("Should handle different keywords correctly")
        void searchUsers_ShouldHandleDifferentKeywords(String keyword) {
            // Given
            int page = 0;
            List<User> users = List.of(testUser);
            List<UserAdminDto> userAdminDtos = List.of(testUserAdminDto);
            PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname"));

            when(userService.searchByKeyword(keyword, pageRequest)).thenReturn(users);
            when(userMapper.toAdminDto(users)).thenReturn(userAdminDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = userController.searchUsers(keyword, page);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).searchByKeyword(keyword, pageRequest);
        }
    }

    @Nested
    @DisplayName("searchUsersCount Tests")
    class SearchUsersCountTests {

        @Test
        @DisplayName("Should return correct count when users match keyword")
        void searchUsersCount_ShouldReturnCorrectCount_WhenUsersMatch() {
            // Given
            String keyword = "test";
            long expectedCount = 5L;

            when(userService.countByKeyword(keyword)).thenReturn(expectedCount);

            // When
            ResponseEntity<Long> response = userController.searchUsersCount(keyword);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedCount);
            verify(userService).countByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return zero when no users match keyword")
        void searchUsersCount_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";

            when(userService.countByKeyword(keyword)).thenReturn(0L);

            // When
            ResponseEntity<Long> response = userController.searchUsersCount(keyword);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(0L);
            verify(userService).countByKeyword(keyword);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "test", "TEST", "user123"})
        @DisplayName("Should handle different keywords correctly")
        void searchUsersCount_ShouldHandleDifferentKeywords(String keyword) {
            // Given
            long expectedCount = 3L;

            when(userService.countByKeyword(keyword)).thenReturn(expectedCount);

            // When
            ResponseEntity<Long> response = userController.searchUsersCount(keyword);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedCount);
            verify(userService).countByKeyword(keyword);
        }
    }

    @Nested
    @DisplayName("searchUsersPageCount Tests")
    class SearchUsersPageCountTests {

        @Test
        @DisplayName("Should calculate correct page count for search results")
        void searchUsersPageCount_ShouldCalculateCorrectPageCount() {
            // Given
            String keyword = "test";
            long totalUsers = 25L;
            long expectedPageCount = 3L; // Math.ceil(25/10)

            when(userService.countByKeyword(keyword)).thenReturn(totalUsers);

            // When
            ResponseEntity<Long> response = userController.searchUsersPageCount(keyword);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedPageCount);
            verify(userService).countByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return zero pages when no users match")
        void searchUsersPageCount_ShouldReturnZero_WhenNoUsersMatch() {
            // Given
            String keyword = "nomatch";

            when(userService.countByKeyword(keyword)).thenReturn(0L);

            // When
            ResponseEntity<Long> response = userController.searchUsersPageCount(keyword);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(0L);
            verify(userService).countByKeyword(keyword);
        }

        @ParameterizedTest
        @ValueSource(longs = {1, 9, 10, 11, 20, 100})
        @DisplayName("Should calculate correct page count for different user counts")
        void searchUsersPageCount_ShouldCalculateCorrectPageCount_ForDifferentCounts(long userCount) {
            // Given
            String keyword = "test";
            long expectedPageCount = (long) Math.ceil((double) userCount / 10);

            when(userService.countByKeyword(keyword)).thenReturn(userCount);

            // When
            ResponseEntity<Long> response = userController.searchUsersPageCount(keyword);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(expectedPageCount);
            verify(userService).countByKeyword(keyword);
        }
    }
}
