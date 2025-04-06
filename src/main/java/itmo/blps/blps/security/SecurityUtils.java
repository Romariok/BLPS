package itmo.blps.blps.security;

import itmo.blps.blps.model.User;
import itmo.blps.blps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Utility class for security operations
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get the username of the current authenticated user
     * @return the username
     * @throws SecurityException if no user is authenticated
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("No authenticated user found");
        }
        return authentication.getName();
    }

    /**
     * Get the current authenticated user entity
     * @return the User entity
     * @throws SecurityException if no user is authenticated
     * @throws UsernameNotFoundException if the user is not found in the database
     */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Get the current authenticated user ID
     * @return the user ID
     * @throws SecurityException if no user is authenticated
     * @throws UsernameNotFoundException if the user is not found in the database
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if the authenticated user matches the requested user ID
     * @param userId the user ID to check
     * @return true if the current user's ID matches the requested ID
     */
    public boolean isCurrentUser(Long userId) {
        try {
            return getCurrentUserId().equals(userId);
        } catch (SecurityException e) {
            return false;
        }
    }
} 