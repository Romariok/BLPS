package itmo.blps.blps.service;

import itmo.blps.blps.dto.RegisterRequestDTO;
import itmo.blps.blps.model.User;
import itmo.blps.blps.dto.LoginRequestDTO;
import itmo.blps.blps.dto.AuthResponseDTO;
import itmo.blps.blps.security.JWTUtil;
import lombok.RequiredArgsConstructor;
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
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if username already exists
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        userService.saveUser(user);

        // Generate token with default student role and permissions
        String token = generateTokenForUser(request.getUsername());

        return new AuthResponseDTO(token, user.getUsername());
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Set authentication in context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate token with all user roles and permissions
        String token = generateTokenForUser(request.getUsername());

        return new AuthResponseDTO(token, request.getUsername());
    }

    private String generateTokenForUser(String username) {
        // Load user details which will contain all authorities (roles and permissions)
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Extract roles and permissions from authorities
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Generate token with all authorities
        return jwtUtil.generateToken(username, authorities);
    }
}