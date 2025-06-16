package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.constant.ConfigField;
import fans.goldenglow.plumaspherebackend.entity.Config;
import fans.goldenglow.plumaspherebackend.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fans.goldenglow.plumaspherebackend.constant.RedisKey.INITIALIZATION_CODE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private ConfigService configService;

    private Config blogTitleConfig;
    private Config blogSubtitleConfig;
    private Config pageSizeConfig;
    private Config initializedConfig;
    private Config versionConfig;

    @BeforeEach
    void setUp() {
        blogTitleConfig = new Config("blog_title", "My Blog", true);
        blogTitleConfig.setId(1L);

        blogSubtitleConfig = new Config("blog_subtitle", "A wonderful blog", true);
        blogSubtitleConfig.setId(2L);

        pageSizeConfig = new Config("page_size", "10", true);
        pageSizeConfig.setId(3L);

        initializedConfig = new Config("initialized", "true", true);
        initializedConfig.setId(4L);

        versionConfig = new Config("config_version", "1", false);
        versionConfig.setId(5L);
    }

    @Test
    void get_ShouldReturnConfigValue_WhenConfigExists() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.of(blogTitleConfig));

        // When
        Optional<String> result = configService.get(ConfigField.BLOG_TITLE);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("My Blog");
        verify(configRepository).findByConfigKey("blog_title");
    }

    @Test
    void get_ShouldReturnEmpty_WhenConfigDoesNotExist() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());

        // When
        Optional<String> result = configService.get(ConfigField.BLOG_TITLE);

        // Then
        assertThat(result).isEmpty();
        verify(configRepository).findByConfigKey("blog_title");
    }

    @Test
    void getAll_ShouldReturnAllConfigs() {
        // Given
        List<Config> allConfigs = Arrays.asList(blogTitleConfig, blogSubtitleConfig, pageSizeConfig);
        when(configRepository.findAll()).thenReturn(allConfigs);

        // When
        List<Config> result = configService.getAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(blogTitleConfig, blogSubtitleConfig, pageSizeConfig);
        verify(configRepository).findAll();
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoConfigs() {
        // Given
        when(configRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Config> result = configService.getAll();

        // Then
        assertThat(result).isEmpty();
        verify(configRepository).findAll();
    }

    @Test
    void getAllPublic_ShouldReturnOnlyPublicConfigs() {
        // Given
        List<Config> allConfigs = Arrays.asList(blogTitleConfig, versionConfig, pageSizeConfig);
        when(configRepository.findAll()).thenReturn(allConfigs);

        // When
        List<Config> result = configService.getAllPublic();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(blogTitleConfig, pageSizeConfig);
        assertThat(result).doesNotContain(versionConfig);
        verify(configRepository).findAll();
    }

    @Test
    void getAllPublic_ShouldReturnEmptyList_WhenNoPublicConfigs() {
        // Given
        List<Config> allConfigs = Collections.singletonList(versionConfig);
        when(configRepository.findAll()).thenReturn(allConfigs);

        // When
        List<Config> result = configService.getAllPublic();

        // Then
        assertThat(result).isEmpty();
        verify(configRepository).findAll();
    }

    @Test
    void set_ShouldCreateNewConfig_WhenConfigDoesNotExist() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.empty());

        // When
        configService.set(ConfigField.BLOG_TITLE, "New Blog Title");

        // Then
        verify(configRepository).findByConfigKey("blog_title");
        verify(configRepository, times(2)).save(any(Config.class)); // One for blog_title, one for version
        verify(configRepository).findByConfigKey("config_version");
    }

    @Test
    void set_ShouldUpdateExistingConfig_WhenConfigExists() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.of(blogTitleConfig));
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.of(versionConfig));

        // When
        configService.set(ConfigField.BLOG_TITLE, "Updated Blog Title");

        // Then
        verify(configRepository).findByConfigKey("blog_title");
        verify(configRepository, times(2)).save(any(Config.class)); // One for blog_title, one for version
        verify(configRepository).findByConfigKey("config_version");
        assertThat(blogTitleConfig.getConfigValue()).isEqualTo("Updated Blog Title");
    }

    @Test
    void set_ShouldNotSetInitialized_WhenAlreadyInitialized() {
        // Given
        when(configRepository.findByConfigKey("initialized")).thenReturn(Optional.of(initializedConfig));

        // When
        configService.set(ConfigField.INITIALIZED, "true");

        // Then
        verify(configRepository).findByConfigKey("initialized");
        verify(configRepository, never()).save(any(Config.class));
        verify(configRepository, never()).findByConfigKey("config_version"); // No version increment
    }

    @Test
    void set_ShouldSetInitialized_WhenNotYetInitialized() {
        // Given
        when(configRepository.findByConfigKey("initialized")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.empty());

        // When
        configService.set(ConfigField.INITIALIZED, "true");

        // Then
        verify(configRepository, times(2)).findByConfigKey("initialized"); // Called twice - once for check, once for general logic
        verify(configRepository, times(2)).save(any(Config.class)); // One for initialized, one for version
        verify(configRepository).findByConfigKey("config_version");
    }

    @Test
    void set_ShouldNotModifyImmutableFields() {
        // When
        configService.set(ConfigField.CONFIG_VERSION, "999");

        // Then
        verify(configRepository, never()).save(any(Config.class));
        verify(configRepository, never()).findByConfigKey(anyString());
    }

    @Test
    void incrementConfigVersion_ShouldCreateVersionConfig_WhenNotExists() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.empty());

        // When
        configService.set(ConfigField.BLOG_TITLE, "New Title");

        // Then
        verify(configRepository, times(2)).save(any(Config.class)); // One for blog_title, one for version
        verify(configRepository).findByConfigKey("blog_title");
        verify(configRepository).findByConfigKey("config_version");
    }

    @Test
    void incrementConfigVersion_ShouldIncrementExistingVersion() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.of(versionConfig));

        // When
        configService.set(ConfigField.BLOG_TITLE, "New Title");

        // Then
        verify(configRepository, times(2)).save(any(Config.class));
        assertThat(versionConfig.getConfigValue()).isEqualTo("2"); // Incremented from 1 to 2
    }

    @Test
    void incrementConfigVersion_ShouldHandleInvalidVersionFormat() {
        // Given
        Config invalidVersionConfig = new Config("config_version", "invalid", false);
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.of(invalidVersionConfig));

        // When
        configService.set(ConfigField.BLOG_TITLE, "New Title");

        // Then
        verify(configRepository, times(2)).save(any(Config.class));
        assertThat(invalidVersionConfig.getConfigValue()).isEqualTo("1"); // Reset to 1 due to invalid format
    }

    @Test
    void checkVerificationCode_ShouldReturnTrue_WhenCodesMatch() {
        // Given
        String testCode = "123456";
        when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn(testCode);

        // When
        boolean result = configService.checkVerificationCode(testCode);

        // Then
        assertThat(result).isTrue();
        verify(redisService).get(INITIALIZATION_CODE_KEY);
    }

    @Test
    void checkVerificationCode_ShouldReturnFalse_WhenCodesDoNotMatch() {
        // Given
        String storedCode = "123456";
        String providedCode = "654321";
        when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn(storedCode);

        // When
        boolean result = configService.checkVerificationCode(providedCode);

        // Then
        assertThat(result).isFalse();
        verify(redisService).get(INITIALIZATION_CODE_KEY);
    }

    @Test
    void checkVerificationCode_ShouldThrowException_WhenCodeNotSetInRedis() {
        // Given
        when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> configService.checkVerificationCode("123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Initialization code is not set yet.");

        verify(redisService).get(INITIALIZATION_CODE_KEY);
    }

    @Test
    void checkVerificationCode_ShouldHandleNullInput() {
        // Given
        when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn("123456");

        // When
        boolean result = configService.checkVerificationCode(null);

        // Then
        assertThat(result).isFalse();
        verify(redisService).get(INITIALIZATION_CODE_KEY);
    }

    @Test
    void checkVerificationCode_ShouldHandleEmptyInput() {
        // Given
        when(redisService.get(INITIALIZATION_CODE_KEY)).thenReturn("123456");

        // When
        boolean result = configService.checkVerificationCode("");

        // Then
        assertThat(result).isFalse();
        verify(redisService).get(INITIALIZATION_CODE_KEY);
    }

    @Test
    void set_ShouldHandleNullValue() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.empty());

        // When
        configService.set(ConfigField.BLOG_TITLE, null);

        // Then
        verify(configRepository, times(2)).save(any(Config.class));
    }

    @Test
    void set_ShouldHandleEmptyValue() {
        // Given
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.empty());

        // When
        configService.set(ConfigField.BLOG_TITLE, "");

        // Then
        verify(configRepository, times(2)).save(any(Config.class));
    }

    @Test
    void incrementConfigVersion_ShouldHandleNullVersionValue() {
        // Given
        Config nullVersionConfig = new Config("config_version", null, false);
        when(configRepository.findByConfigKey("blog_title")).thenReturn(Optional.empty());
        when(configRepository.findByConfigKey("config_version")).thenReturn(Optional.of(nullVersionConfig));

        // When
        configService.set(ConfigField.BLOG_TITLE, "New Title");

        // Then
        verify(configRepository, times(2)).save(any(Config.class));
        assertThat(nullVersionConfig.getConfigValue()).isEqualTo("1"); // Should handle null gracefully
    }

    @Test
    void getAllPublic_ShouldHandleMixedPublicPrivateConfigs() {
        // Given
        Config privateConfig1 = new Config("private1", "value1", false);
        Config privateConfig2 = new Config("private2", "value2", false);
        Config publicConfig1 = new Config("public1", "value1", true);
        Config publicConfig2 = new Config("public2", "value2", true);

        List<Config> allConfigs = Arrays.asList(privateConfig1, publicConfig1, privateConfig2, publicConfig2);
        when(configRepository.findAll()).thenReturn(allConfigs);

        // When
        List<Config> result = configService.getAllPublic();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Config::getConfigKey).containsExactlyInAnyOrder("public1", "public2");
        assertThat(result).extracting(Config::getIsOpenToPublic).containsOnly(true);
    }
}
