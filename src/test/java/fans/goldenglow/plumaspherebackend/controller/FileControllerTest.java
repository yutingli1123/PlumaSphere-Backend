package fans.goldenglow.plumaspherebackend.controller;

import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import fans.goldenglow.plumaspherebackend.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@ImportAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JpaBaseConfiguration.class})
@Import(fans.goldenglow.plumaspherebackend.config.TestSecurityConfig.class)
@DisplayName("FileController API Tests")
class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Nested
    @DisplayName("/upload Endpoint")
    class UploadEndpoint {
        @Test
        @DisplayName("Should upload single file successfully")
        void uploadSingleFileSuccess() throws Exception, FileSaveException {
            MockMultipartFile file = new MockMultipartFile("file[]", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy content".getBytes());
            Mockito.doAnswer((Answer<Object>) invocation -> "/upload/test.jpg").when(fileService).saveFile(any());

            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/file/upload")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.succMap['test.jpg']").value("/upload/test.jpg"))
                    .andExpect(jsonPath("$.data.errFiles", hasSize(0)));
        }

        @Test
        @DisplayName("Should upload multiple files with some failures")
        void uploadMultipleFilesWithFailures() throws Exception, FileSaveException {
            MockMultipartFile file1 = new MockMultipartFile("file[]", "ok.jpg", MediaType.IMAGE_JPEG_VALUE, "ok".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file[]", "fail.jpg", MediaType.IMAGE_JPEG_VALUE, "fail".getBytes());
            Mockito.doAnswer((Answer<Object>) invocation -> {
                Object arg = invocation.getArgument(0);
                if (arg instanceof MultipartFile) {
                    String name = ((MultipartFile) arg).getOriginalFilename();
                    if ("ok.jpg".equals(name)) return "/upload/ok.jpg";
                    if ("fail.jpg".equals(name)) throw new FileSaveException("fail.jpg");
                }
                return null;
            }).when(fileService).saveFile(any());

            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/file/upload")
                            .file(file1)
                            .file(file2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.succMap['ok.jpg']").value("/upload/ok.jpg"))
                    .andExpect(jsonPath("$.data.errFiles", hasItem("fail.jpg")));
        }

        @Test
        @DisplayName("Should handle all files failing to upload")
        void uploadAllFilesFail() throws Exception, FileSaveException {
            MockMultipartFile file1 = new MockMultipartFile("file[]", "fail1.jpg", MediaType.IMAGE_JPEG_VALUE, "fail1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("file[]", "fail2.jpg", MediaType.IMAGE_JPEG_VALUE, "fail2".getBytes());
            Mockito.doAnswer((Answer<Object>) invocation -> {
                Object arg = invocation.getArgument(0);
                if (arg instanceof MultipartFile) {
                    String name = ((MultipartFile) arg).getOriginalFilename();
                    if ("fail1.jpg".equals(name)) throw new FileSaveException("fail1.jpg");
                    if ("fail2.jpg".equals(name)) throw new FileSaveException("fail2.jpg");
                }
                return null;
            }).when(fileService).saveFile(any());

            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/file/upload")
                            .file(file1)
                            .file(file2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.succMap").isEmpty())
                    .andExpect(jsonPath("$.data.errFiles", containsInAnyOrder("fail1.jpg", "fail2.jpg")));
        }

        @Test
        @DisplayName("Should skip files returning null from service")
        void uploadFileReturnsNull() throws Exception, FileSaveException {
            MockMultipartFile file = new MockMultipartFile("file[]", "null.jpg", MediaType.IMAGE_JPEG_VALUE, "null".getBytes());
            Mockito.doAnswer((Answer<Object>) invocation -> null).when(fileService).saveFile(any());

            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/file/upload")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.succMap").isEmpty())
                    .andExpect(jsonPath("$.data.errFiles", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("/fetch Endpoint")
    class FetchEndpoint {
        @Test
        @DisplayName("Should fetch image successfully")
        void fetchImageSuccess() throws Exception, FileSaveException {
            String originalUrl = "https://example.com/image.jpg";
            String localUrl = "/upload/image.jpg";
            Mockito.doAnswer((Answer<Object>) invocation -> localUrl).when(fileService).fetchImage(eq(originalUrl));

            mockMvc.perform(post("/api/v1/file/fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":\"" + originalUrl + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.originalURL").value(originalUrl))
                    .andExpect(jsonPath("$.data.url").value(localUrl));
        }

        @Test
        @DisplayName("Should handle fetch image failure (service throws exception)")
        void fetchImageFailure() throws Exception, FileSaveException {
            String originalUrl = "https://example.com/image.jpg";
            Mockito.doAnswer((Answer<Object>) invocation -> {
                throw new FileSaveException(originalUrl);
            }).when(fileService).fetchImage(eq(originalUrl));

            mockMvc.perform(post("/api/v1/file/fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":\"" + originalUrl + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.msg").value("download image failed"))
                    .andExpect(jsonPath("$.data.originalURL").value(originalUrl))
                    .andExpect(jsonPath("$.data.url").value(""));
        }

        @Test
        @DisplayName("Should handle fetch image failure (service throws generic exception)")
        void fetchImageGenericFailure() throws Exception, FileSaveException {
            String originalUrl = "https://example.com/image.jpg";
            Mockito.doAnswer((Answer<Object>) invocation -> {
                throw new RuntimeException("fail");
            }).when(fileService).fetchImage(eq(originalUrl));

            mockMvc.perform(post("/api/v1/file/fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":\"" + originalUrl + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.msg").value("download image failed"))
                    .andExpect(jsonPath("$.data.originalURL").value(originalUrl))
                    .andExpect(jsonPath("$.data.url").value(""));
        }

        @Test
        @DisplayName("Should handle missing url in request body")
        void fetchImageMissingUrl() throws Exception {
            mockMvc.perform(post("/api/v1/file/fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.msg").value("download image failed"))
                    .andExpect(jsonPath("$.data.originalURL").doesNotExist())
                    .andExpect(jsonPath("$.data.url").value(""));
        }

        @Test
        @DisplayName("Should return 400 for invalid JSON in fetch API")
        void fetchImageInvalidJson() throws Exception {
            mockMvc.perform(post("/api/v1/file/fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("\"not-a-json\""))
                    .andExpect(status().isBadRequest());
        }
    }
}
