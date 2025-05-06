package itmo.blps.blps.security;

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

import itmo.blps.blps.filter.ActivityTrackingFilter;
import itmo.blps.blps.model.Permission;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Slf4j
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ActivityTrackingFilter activityTrackingFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/camunda/**").permitAll()
                        // Course-related endpoints with permission checks
                        .requestMatchers("/api/courses/new").hasAuthority(Permission.CREATE_COURSE.name())
                        .requestMatchers("/api/courses/*/edit").hasAuthority(Permission.EDIT_COURSE.name())
                        .requestMatchers("/api/courses/*/delete").hasAuthority(Permission.DELETE_COURSE.name())
                        .requestMatchers("/api/courses/enroll").hasAuthority(Permission.VIEW_COURSE.name())
                        .requestMatchers("/api/courses").hasAuthority(Permission.VIEW_COURSE.name())

                        // Task-related endpoints with permission checks
                        .requestMatchers("/api/tasks/submit").hasAuthority(Permission.SUBMIT_TASK.name())
                        .requestMatchers("/api/tasks/score").hasAuthority(Permission.GRADE_TASK.name())
                        .requestMatchers("/api/tasks/*/unscored").hasAuthority(Permission.GRADE_TASK.name())
                        .requestMatchers("/api/tasks/*/submissions").hasAuthority(Permission.VIEW_TASK.name())
                        .requestMatchers("/api/tasks/*").hasAuthority(Permission.VIEW_TASK.name())
                        .requestMatchers("/api/tasks").hasAuthority(Permission.VIEW_TASK.name())

                        // Certificate-related endpoints with permission checks
                        .requestMatchers("/api/certificates/request").hasAuthority(Permission.VIEW_CERTIFICATE.name())
                        .requestMatchers("/api/certificates/status/*").hasAuthority(Permission.VIEW_CERTIFICATE.name())
                        .requestMatchers("/api/certificates/course/*/pending")
                        .hasAuthority(Permission.ISSUE_CERTIFICATE.name())
                        .requestMatchers("/api/certificates/process").hasAuthority(Permission.ISSUE_CERTIFICATE.name())
                        .requestMatchers("/api/certificates/verify").hasAuthority(Permission.VERIFY_CERTIFICATE.name())

                        // Swagger UI and API docs
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Require authentication for any other request
                        .anyRequest().authenticated())
                        
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(activityTrackingFilter, JwtAuthenticationFilter.class);

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
