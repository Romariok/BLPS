package itmo.blps.blps.security;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JWTUtil {
    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration}")
    private long expirationTime;
    
    private Key secretKey;
    
    @PostConstruct
    public void init() {
        try {
            // For production, use a properly generated key from application properties
            // If the key is too short, generate a secure key using Keys.secretKeyFor
            try {
                // Try to use the provided secret key
                secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyString));
                log.info("Using provided JWT secret key");
            } catch (Exception e) {
                // If the provided key is invalid or too weak, generate a secure one
                log.warn("Provided JWT secret key is invalid or too weak. Generating a secure key...");
                secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                log.info("Generated secure JWT secret key");
            }
        } catch (Exception e) {
            log.error("Error initializing JWT secret key", e);
            // Fallback to a secure key
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            log.info("Using fallback secure JWT secret key");
        }
    }

    // Legacy method - kept for backward compatibility
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Enhanced method that supports all authorities (roles and permissions)
    public String generateToken(String username, Set<String> authorities) {
        String authoritiesString = String.join(",", authorities);
        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", authoritiesString)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public Set<String> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String authorities = claims.get("authorities", String.class);
        return Set.of(authorities.split(","));
    }

    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
