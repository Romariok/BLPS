package itmo.blps.blps.service;

import itmo.blps.blps.dto.RegisterRequestDTO;
import itmo.blps.blps.model.User;
import itmo.blps.blps.dto.LoginRequestDTO;
import itmo.blps.blps.dto.AuthResponseDTO;
import itmo.blps.blps.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username already exists
        if (userService.existsByUsername(request.getUsername())) {
            log.warn("Username already taken: {}", request.getUsername());
            throw new RuntimeException("Username is already taken");
        }

        try {
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            // Save user
            userService.saveUser(user);
            log.info("User saved successfully: {}", user.getUsername());

            // Generate token with default student role and permissions
            String token = generateTokenForUser(request.getUsername());
            log.info("Token generated for new user: {}", user.getUsername());

            return new AuthResponseDTO(token, user.getUsername());
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Processing login for user: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            // Set authentication in context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("User authenticated successfully: {}", request.getUsername());

            // Generate token with all user roles and permissions
            String token = generateTokenForUser(request.getUsername());
            log.info("Token generated for user: {}", request.getUsername());

            return new AuthResponseDTO(token, request.getUsername());
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    private String generateTokenForUser(String username) {
        log.debug("Generating token for user: {}", username);
        try {
            // Load user details which will contain all authorities (roles and permissions)
            UserDetails userDetails = userService.loadUserByUsername(username);

            // Extract roles and permissions from authorities
            Set<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            log.debug("User {} has authorities: {}", username, authorities);

            // Generate token with all authorities
            return jwtUtil.generateToken(username, authorities);
        } catch (Exception e) {
            log.error("Error generating token for user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
}