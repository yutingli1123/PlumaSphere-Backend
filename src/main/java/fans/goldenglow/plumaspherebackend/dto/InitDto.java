package fans.goldenglow.plumaspherebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InitDto {
    @JsonProperty("verification_code")
    public String verificationCode;
    @JsonProperty("blog_title")
    public String blogTitle;
    @JsonProperty("blog_subtitle")
    public String blogSubtitle;
    @JsonProperty("admin_username")
    public String adminUsername;
    @JsonProperty("admin_password")
    public String adminPassword;
}
