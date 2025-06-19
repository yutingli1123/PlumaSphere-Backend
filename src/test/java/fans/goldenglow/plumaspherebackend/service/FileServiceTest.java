package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.exceptions.FileSaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Tests")
class FileServiceTest {

    private static final String TEST_SERVER_ADDRESS = "https://example.com";
    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(TEST_SERVER_ADDRESS);
    }

    @Nested
    @DisplayName("URL Validation")
    class UrlValidationTests {

        @Test
        @DisplayName("Should return true for valid https url")
        void checkURLValidation_ShouldReturnTrue_ForValidHttpsUrl() {
            // Given
            String validUrl = "https://example.com/image.jpg";

            // When
            boolean result = fileService.checkURLValidation(validUrl);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true for valid http url")
        void checkURLValidation_ShouldReturnTrue_ForValidHttpUrl() {
            // Given
            String validUrl = "http://example.com/image.jpg";

            // When
            boolean result = fileService.checkURLValidation(validUrl);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null url")
        void checkURLValidation_ShouldReturnFalse_ForNullUrl() {
            // When
            boolean result = fileService.checkURLValidation(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty url")
        void checkURLValidation_ShouldReturnFalse_ForEmptyUrl() {
            // When
            boolean result = fileService.checkURLValidation("");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for invalid scheme")
        void checkURLValidation_ShouldReturnFalse_ForInvalidScheme() {
            // Given
            String invalidUrl = "ftp://example.com/file.txt";

            // When
            boolean result = fileService.checkURLValidation(invalidUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for url without scheme")
        void checkURLValidation_ShouldReturnFalse_ForUrlWithoutScheme() {
            // Given
            String invalidUrl = "example.com/image.jpg";

            // When
            boolean result = fileService.checkURLValidation(invalidUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for url without host")
        void checkURLValidation_ShouldReturnFalse_ForUrlWithoutHost() {
            // Given
            String invalidUrl = "https:///image.jpg";

            // When
            boolean result = fileService.checkURLValidation(invalidUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for localhost url")
        void checkURLValidation_ShouldReturnFalse_ForLocalhostUrl() {
            // Given
            String localhostUrl = "http://localhost/image.jpg";

            // When
            boolean result = fileService.checkURLValidation(localhostUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for 127.0.0.1 url")
        void checkURLValidation_ShouldReturnFalse_For127001Url() {
            // Given
            String localhostUrl = "http://127.0.0.1/image.jpg";

            // When
            boolean result = fileService.checkURLValidation(localhostUrl);

            // Then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "http://192.168.1.1/image.jpg",
                "http://10.0.0.1/image.jpg",
                "http://172.16.0.1/image.jpg"
        })
        @DisplayName("Should return false for private network urls")
        void checkURLValidation_ShouldReturnFalse_ForPrivateNetworkUrls(String privateUrl) {
            // When
            boolean result = fileService.checkURLValidation(privateUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for malformed url")
        void checkURLValidation_ShouldReturnFalse_ForMalformedUrl() {
            // Given
            String malformedUrl = "not-a-url";

            // When
            boolean result = fileService.checkURLValidation(malformedUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle complex urls")
        void checkURLValidation_ShouldHandleComplexUrls() {
            // Given
            String complexUrl = "https://example.com:8080/path/to/image.jpg?param=value#fragment";

            // When
            boolean result = fileService.checkURLValidation(complexUrl);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle urls with special characters")
        void checkURLValidation_ShouldHandleUrlsWithSpecialCharacters() {
            // Given
            String urlWithSpecialChars = "https://example.com/path/image%20name.jpg";

            // When
            boolean result = fileService.checkURLValidation(urlWithSpecialChars);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for file protocol")
        void checkURLValidation_ShouldReturnFalse_ForFileProtocol() {
            // Given
            String fileUrl = "file:///path/to/file.jpg";

            // When
            boolean result = fileService.checkURLValidation(fileUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for javascript protocol")
        void checkURLValidation_ShouldReturnFalse_ForJavaScriptProtocol() {
            // Given
            String jsUrl = "javascript:alert('xss')";

            // When
            boolean result = fileService.checkURLValidation(jsUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for data protocol")
        void checkURLValidation_ShouldReturnFalse_ForDataProtocol() {
            // Given
            String dataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

            // When
            boolean result = fileService.checkURLValidation(dataUrl);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle case insensitive schemes")
        void checkURLValidation_ShouldHandleCaseInsensitiveSchemes() {
            // Given
            String upperCaseUrl = "HTTPS://example.com/image.jpg";
            String mixedCaseUrl = "HtTpS://example.com/image.jpg";

            // When
            boolean upperResult = fileService.checkURLValidation(upperCaseUrl);
            boolean mixedResult = fileService.checkURLValidation(mixedCaseUrl);

            // Then
            assertThat(upperResult).isTrue();
            assertThat(mixedResult).isTrue();
        }
    }

    @Nested
    @DisplayName("File Save Operations")
    class FileSaveTests {

        @Test
        @DisplayName("Should throw exception when file has no original filename")
        void saveFile_ShouldThrowException_WhenFileHasNoOriginalFilename() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> fileService.saveFile(mockFile))
                    .isInstanceOf(FileSaveException.class);
        }

        @Test
        @DisplayName("Should throw exception when file has empty original filename")
        void saveFile_ShouldThrowException_WhenFileHasEmptyOriginalFilename() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("");

            // When & Then
            assertThatThrownBy(() -> fileService.saveFile(mockFile))
                    .isInstanceOf(FileSaveException.class);
        }

        @Test
        @DisplayName("Should generate valid url when file has valid name")
        void saveFile_ShouldGenerateValidUrl_WhenFileHasValidName() throws IOException, FileSaveException {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

            // When
            String result = fileService.saveFile(mockFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).startsWith(TEST_SERVER_ADDRESS + "/upload/");
            assertThat(result).endsWith(".jpg");
        }

        @Test
        @DisplayName("Should generate valid url when file has no extension")
        void saveFile_ShouldGenerateValidUrl_WhenFileHasNoExtension() throws IOException, FileSaveException {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("test");
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

            // When
            String result = fileService.saveFile(mockFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).startsWith(TEST_SERVER_ADDRESS + "/upload/");
            // Should not end with an extension (no dot followed by alphanumeric characters at the end)
            assertThat(result).doesNotMatch(".*\\.[a-zA-Z0-9]+$");
        }

        @Test
        @DisplayName("Should throw exception when input stream throws exception")
        void saveFile_ShouldThrowException_WhenInputStreamThrowsException() throws IOException {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile.getInputStream()).thenThrow(new IOException("Test exception"));

            // When & Then
            assertThatThrownBy(() -> fileService.saveFile(mockFile))
                    .isInstanceOf(FileSaveException.class);
        }

        @Test
        @DisplayName("Should handle different file extensions")
        void saveFile_ShouldHandleDifferentFileExtensions() throws IOException, FileSaveException {
            // Given
            String[] extensions = {"jpg", "png", "pdf", "txt", "doc"};

            for (String ext : extensions) {
                MultipartFile mockFile = mock(MultipartFile.class);
                when(mockFile.getOriginalFilename()).thenReturn("test." + ext);
                when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

                // When
                String result = fileService.saveFile(mockFile);

                // Then
                assertThat(result).endsWith("." + ext);
                assertThat(result).startsWith(TEST_SERVER_ADDRESS + "/upload/");
            }
        }

        @Test
        @DisplayName("Should generate unique filenames")
        void saveFile_ShouldGenerateUniqueFilenames() throws IOException, FileSaveException {
            // Given
            MultipartFile mockFile1 = mock(MultipartFile.class);
            when(mockFile1.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile1.getInputStream()).thenReturn(new ByteArrayInputStream("test content 1".getBytes()));

            MultipartFile mockFile2 = mock(MultipartFile.class);
            when(mockFile2.getOriginalFilename()).thenReturn("test.jpg");
            when(mockFile2.getInputStream()).thenReturn(new ByteArrayInputStream("test content 2".getBytes()));

            // When
            String result1 = fileService.saveFile(mockFile1);
            String result2 = fileService.saveFile(mockFile2);

            // Then
            assertThat(result1).isNotEqualTo(result2);
            assertThat(result1).startsWith(TEST_SERVER_ADDRESS + "/upload/");
            assertThat(result2).startsWith(TEST_SERVER_ADDRESS + "/upload/");
        }
    }

    @Nested
    @DisplayName("Image Fetch Operations")
    class ImageFetchTests {

        @Test
        @DisplayName("Should throw exception when url is invalid")
        void fetchImage_ShouldThrowException_WhenUrlIsInvalid() {
            // Given
            String invalidUrl = "invalid-url";

            // When & Then
            assertThatThrownBy(() -> fileService.fetchImage(invalidUrl))
                    .isInstanceOf(FileSaveException.class);
        }

        @Test
        @DisplayName("Should throw exception when url is localhost")
        void fetchImage_ShouldThrowException_WhenUrlIsLocalhost() {
            // Given
            String localhostUrl = "http://localhost/image.jpg";

            // When & Then
            assertThatThrownBy(() -> fileService.fetchImage(localhostUrl))
                    .isInstanceOf(FileSaveException.class);
        }

        @Test
        @DisplayName("Should throw exception when url is null")
        void fetchImage_ShouldThrowException_WhenUrlIsNull() {
            // When & Then
            assertThatThrownBy(() -> fileService.fetchImage(null))
                    .isInstanceOf(FileSaveException.class);
        }

        @Test
        @DisplayName("Should throw exception when url is empty")
        void fetchImage_ShouldThrowException_WhenUrlIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> fileService.fetchImage(""))
                    .isInstanceOf(FileSaveException.class);
        }
    }
}
