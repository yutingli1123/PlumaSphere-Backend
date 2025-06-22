package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.dto.BanRequestDto;
import fans.goldenglow.plumaspherebackend.service.BannedIpService;
import fans.goldenglow.plumaspherebackend.service.UserBanService;
import fans.goldenglow.plumaspherebackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;

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
    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
            when(dto.getUserId()).thenThrow(new RuntimeException("fail"));
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
}
