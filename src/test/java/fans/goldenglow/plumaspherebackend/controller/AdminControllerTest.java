package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.BanRequestDto;
import fans.goldenglow.plumaspherebackend.dto.UserAdminDto;
import fans.goldenglow.plumaspherebackend.entity.BannedIp;
import fans.goldenglow.plumaspherebackend.entity.User;
import fans.goldenglow.plumaspherebackend.mapper.UserMapper;
import fans.goldenglow.plumaspherebackend.service.BannedIpService;
import fans.goldenglow.plumaspherebackend.service.UserBanService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("AdminController Tests")
class AdminControllerTest {
    @Mock
    private BannedIpService bannedIpService;
    @Mock
    private UserService userService;
    @Mock
    private UserBanService userBanService;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private AdminController adminController;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Nested
    @DisplayName("POST /api/v1/admin/ban-user")
    class BanUser {
        @Test
        @DisplayName("Should ban user permanently")
        void banUser_Permanent() {
            BanRequestDto dto = mock(BanRequestDto.class);
            when(dto.getUserId()).thenReturn(1L);
            when(dto.getReason()).thenReturn("test");
            when(dto.getExpiresAt()).thenReturn(null);
            ResponseEntity<String> response = adminController.banUser(dto);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(userBanService).banUser(1L, "test");
        }

        @Test
        @DisplayName("Should ban user temporarily")
        void banUser_Temporary() {
            BanRequestDto dto = mock(BanRequestDto.class);
            when(dto.getUserId()).thenReturn(2L);
            when(dto.getReason()).thenReturn("temp");
            ZonedDateTime expires = ZonedDateTime.now();
            when(dto.getExpiresAt()).thenReturn(expires);
            ResponseEntity<String> response = adminController.banUser(dto);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(userBanService).banUserTemporary(eq(2L), eq("temp"), any());
        }

        @Test
        @DisplayName("Should return bad request on exception")
        void banUser_Exception() {
            BanRequestDto dto = mock(BanRequestDto.class);
            when(dto.getUserId()).thenReturn(1L);
            when(dto.getReason()).thenReturn("test");
            when(dto.getExpiresAt()).thenReturn(null);

            doThrow(new RuntimeException("fail")).when(userBanService).banUser(1L, "test");
            ResponseEntity<String> response = adminController.banUser(dto);
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/unban-user")
    class UnbanUser {
        @Test
        @DisplayName("Should unban user successfully")
        void unbanUser_Success() {
            ResponseEntity<String> response = adminController.unbanUser(1L);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            verify(userBanService).unbanUser(1L);
        }

        @Test
        @DisplayName("Should return bad request on exception")
        void unbanUser_Exception() {
            doThrow(new RuntimeException("fail")).when(userBanService).unbanUser(2L);
            ResponseEntity<String> response = adminController.unbanUser(2L);
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/banned-users/search")
    class SearchBannedUsers {
        @Test
        @DisplayName("Should return banned users matching keyword")
        void searchBannedUsers_Success() {
            // Given
            String keyword = "test";
            int page = 0;
            User mockUser = mock(User.class);
            UserAdminDto mockDto = mock(UserAdminDto.class);
            Page<User> mockPage = new PageImpl<>(List.of(mockUser));
            List<UserAdminDto> mockDtos = List.of(mockDto);

            when(userBanService.searchBannedUsersByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname"))))
                    .thenReturn(mockPage);
            when(userMapper.toAdminDto(List.of(mockUser))).thenReturn(mockDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = adminController.searchBannedUsers(keyword, page);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(mockDtos);
            verify(userBanService).searchBannedUsersByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname")));
            verify(userMapper).toAdminDto(List.of(mockUser));
        }

        @Test
        @DisplayName("Should return empty list when no banned users match")
        void searchBannedUsers_Empty() {
            // Given
            String keyword = "nomatch";
            int page = 0;
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
            List<UserAdminDto> emptyDtos = Collections.emptyList();

            when(userBanService.searchBannedUsersByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname"))))
                    .thenReturn(emptyPage);
            when(userMapper.toAdminDto(Collections.emptyList())).thenReturn(emptyDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = adminController.searchBannedUsers(keyword, page);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should return banned users count")
        void searchBannedUsersCount_Success() {
            // Given
            String keyword = "test";
            long expectedCount = 5L;

            when(userBanService.countBannedUsersByKeyword(keyword)).thenReturn(expectedCount);

            // When
            ResponseEntity<Long> response = adminController.searchBannedUsersCount(keyword);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(expectedCount);
            verify(userBanService).countBannedUsersByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return banned users page count")
        void searchBannedUsersPageCount_Success() {
            // Given
            String keyword = "test";
            long totalUsers = 25L;
            long expectedPageCount = 3L; // Math.ceil(25/10)

            when(userBanService.countBannedUsersByKeyword(keyword)).thenReturn(totalUsers);

            // When
            ResponseEntity<Long> response = adminController.searchBannedUsersPageCount(keyword);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(expectedPageCount);
            verify(userBanService).countBannedUsersByKeyword(keyword);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/pending-banned-users/search")
    class SearchPendingBannedUsers {
        @Test
        @DisplayName("Should return pending banned users matching keyword")
        void searchPendingBannedUsers_Success() {
            // Given
            String keyword = "test";
            int page = 0;
            User mockUser = mock(User.class);
            UserAdminDto mockDto = mock(UserAdminDto.class);
            Page<User> mockPage = new PageImpl<>(List.of(mockUser));
            List<UserAdminDto> mockDtos = List.of(mockDto);

            when(userBanService.searchPendingBannedUsersByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname"))))
                    .thenReturn(mockPage);
            when(userMapper.toAdminDto(List.of(mockUser))).thenReturn(mockDtos);

            // When
            ResponseEntity<List<UserAdminDto>> response = adminController.searchPendingBannedUsers(keyword, page);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(mockDtos);
            verify(userBanService).searchPendingBannedUsersByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "nickname")));
            verify(userMapper).toAdminDto(List.of(mockUser));
        }

        @Test
        @DisplayName("Should return pending banned users count")
        void searchPendingBannedUsersCount_Success() {
            // Given
            String keyword = "test";
            long expectedCount = 3L;

            when(userBanService.countPendingBannedUsersByKeyword(keyword)).thenReturn(expectedCount);

            // When
            ResponseEntity<Long> response = adminController.searchPendingBannedUsersCount(keyword);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(expectedCount);
            verify(userBanService).countPendingBannedUsersByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return pending banned users page count")
        void searchPendingBannedUsersPageCount_Success() {
            // Given
            String keyword = "test";
            long totalUsers = 15L;
            long expectedPageCount = 2L; // Math.ceil(15/10)

            when(userBanService.countPendingBannedUsersByKeyword(keyword)).thenReturn(totalUsers);

            // When
            ResponseEntity<Long> response = adminController.searchPendingBannedUsersPageCount(keyword);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(expectedPageCount);
            verify(userBanService).countPendingBannedUsersByKeyword(keyword);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/banned-ips/search")
    class SearchBannedIps {
        @Test
        @DisplayName("Should return banned IPs matching keyword")
        void searchBannedIps_Success() {
            // Given
            String keyword = "192.168";
            int page = 0;
            BannedIp mockBannedIp = mock(BannedIp.class);
            List<BannedIp> mockBannedIps = List.of(mockBannedIp);

            when(bannedIpService.searchByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "ipAddress"))))
                    .thenReturn(mockBannedIps);

            // When
            ResponseEntity<List<BannedIp>> response = adminController.searchBannedIps(keyword, page);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(mockBannedIps);
            verify(bannedIpService).searchByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "ipAddress")));
        }

        @Test
        @DisplayName("Should return empty list when no banned IPs match")
        void searchBannedIps_Empty() {
            // Given
            String keyword = "nomatch";
            int page = 0;
            List<BannedIp> emptyList = Collections.emptyList();

            when(bannedIpService.searchByKeyword(keyword, PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "ipAddress"))))
                    .thenReturn(emptyList);

            // When
            ResponseEntity<List<BannedIp>> response = adminController.searchBannedIps(keyword, page);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should return banned IPs count")
        void searchBannedIpsCount_Success() {
            // Given
            String keyword = "192.168";
            long expectedCount = 7L;

            when(bannedIpService.countByKeyword(keyword)).thenReturn(expectedCount);

            // When
            ResponseEntity<Long> response = adminController.searchBannedIpsCount(keyword);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(expectedCount);
            verify(bannedIpService).countByKeyword(keyword);
        }

        @Test
        @DisplayName("Should return banned IPs page count")
        void searchBannedIpsPageCount_Success() {
            // Given
            String keyword = "192.168";
            long totalIps = 35L;
            long expectedPageCount = 4L; // Math.ceil(35/10)

            when(bannedIpService.countByKeyword(keyword)).thenReturn(totalIps);

            // When
            ResponseEntity<Long> response = adminController.searchBannedIpsPageCount(keyword);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isEqualTo(expectedPageCount);
            verify(bannedIpService).countByKeyword(keyword);
        }
    }
}
