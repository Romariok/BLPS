package itmo.blps.blps.filter;

import itmo.blps.blps.service.ActivityTrackingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that tracks user activity on every request.
 * Updates the lastActiveTime for the authenticated user.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityTrackingFilter extends OncePerRequestFilter {

    private final ActivityTrackingService activityTrackingService;

    @Override
    protected void doFilterInternal(
            @SuppressWarnings("null") HttpServletRequest request,
            @SuppressWarnings("null") HttpServletResponse response,
            @SuppressWarnings("null") FilterChain filterChain) throws ServletException, IOException {

        // Execute the filter chain first to ensure the request is processed
        filterChain.doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            
            try {
                // Extract user ID from principal
                String username = authentication.getName();
                
                // Update last active time asynchronously to avoid impacting response time
                new Thread(() -> {
                    try {
                        log.debug("Updating last active time for user: {}", username);
                        activityTrackingService.trackActivity(username);
                    } catch (Exception e) {
                        log.error("Error updating last active time for user: {}", username, e);
                    }
                }).start();
            } catch (Exception e) {
                // Log but don't rethrow to ensure the response is not affected
                log.error("Error in activity tracking filter", e);
            }
        }
    }
} 