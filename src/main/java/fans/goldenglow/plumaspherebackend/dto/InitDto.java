package fans.goldenglow.plumaspherebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

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
