package fans.goldenglow.plumaspherebackend.config.filter;

import fans.goldenglow.plumaspherebackend.service.UserActivityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class UserActivityFilter extends OncePerRequestFilter {
    private final UserActivityService userActivityService;


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                userActivityService.updateUserActivity(authentication);
            }
        } catch (Exception e) {
            log.error("Error updating user activity: {}", e.getMessage());
        } finally {
            filterChain.doFilter(request, response);
        }
    }
}
