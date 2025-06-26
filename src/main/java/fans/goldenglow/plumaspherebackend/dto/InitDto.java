package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for initializing the blog.
 * This class is used to send requests to initialize the blog with various settings.
 */
@Data
@AllArgsConstructor
public class InitDto {
    public String verificationCode;
    public String blogTitle;
    public String blogSubtitle;
    public String adminUsername;
    public String adminPassword;
    public String adminNickname;
}
