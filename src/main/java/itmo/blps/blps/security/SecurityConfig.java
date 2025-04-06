package itmo.blps.blps.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import itmo.blps.blps.model.Permission;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Slf4j
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - make sure to explicitly permit ALL auth endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // Course-related endpoints with permission checks
                        .requestMatchers("/api/courses").hasAuthority(Permission.VIEW_COURSE.name())
                        .requestMatchers("/api/courses/*/enroll").hasAuthority(Permission.VIEW_COURSE.name())
                        .requestMatchers(("/api/courses/new")).hasAuthority(Permission.CREATE_COURSE.name())
                        .requestMatchers("/api/courses/*/edit").hasAuthority(Permission.EDIT_COURSE.name())
                        .requestMatchers("/api/courses/*/delete").hasAuthority(Permission.DELETE_COURSE.name())

                        // Task-related endpoints with permission checks
                        .requestMatchers("/api/tasks").hasAuthority(Permission.VIEW_TASK.name())
                        .requestMatchers("/api/tasks/*/submit").hasAuthority(Permission.SUBMIT_TASK.name())
                        .requestMatchers("/api/tasks/*/grade").hasAuthority(Permission.GRADE_TASK.name())

                        // Certificate-related endpoints with permission checks
                        .requestMatchers("/api/certificates").hasAuthority(Permission.VIEW_CERTIFICATE.name())
                        .requestMatchers("/api/certificates/issue").hasAuthority(Permission.ISSUE_CERTIFICATE.name())
                        .requestMatchers("/api/certificates/verify").hasAuthority(Permission.VERIFY_CERTIFICATE.name())

                        // Swagger UI and API docs
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Require authentication for any other request
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
