package fans.goldenglow.plumaspherebackend.service;

import fans.goldenglow.plumaspherebackend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserActivityService {
    private final UserService userService;
    private final TokenService tokenService;

    public void updateUserActivity(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Long userId = tokenService.extractUserIdFromJwt(jwtToken);
            Optional<User> user = userService.findById(userId);
            if (user.isPresent()) {
                User userEntity = user.get();
                userEntity.updateLastActivity();
                userService.save(userEntity);
            }
        }
    }
}
