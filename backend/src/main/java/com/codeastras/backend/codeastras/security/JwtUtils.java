package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private final Key key;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;

    public JwtUtils(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes());
        this.accessExpiryMs = props.getAccessExpirationMs();
        this.refreshExpiryMs = props.getRefreshExpirationMs();
    }

    // ===========================
    //   GENERATING TOKENS
    // ===========================
    public String generateAccessToken(UUID userId, String username, String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpiryMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String sessionId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpiryMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("sid", sessionId)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===========================
    //   VALIDATION
    // ===========================
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ===========================
    //   GET USER FROM HTTP SECURITY CONTEXT
    // ===========================
    public UUID getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new UnauthorizedException("User not authenticated");

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        if (principal instanceof UUID id) {
            return id;
        }

        if (principal instanceof String s) {
            try {
                return UUID.fromString(s);
            } catch (Exception ignored) {}
        }

        if (principal instanceof UserDetails ud) {
            try {
                return UUID.fromString(ud.getUsername());
            } catch (Exception ignored) {}
        }

        throw new UnauthorizedException("Unable to extract user ID from principal");
    }

    // ===========================
    //   GET USER FROM TOKEN (WS USE-CASE)
    // ===========================
    public UUID getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return UUID.fromString(claims.getSubject());

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid JWT token");
        }
    }

    // ===========================
    //   OTHER HELPERS
    // ===========================
    public String getSessionId(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return c.get("sid", String.class);
    }

    public boolean isRefreshToken(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return "refresh".equals(c.get("type", String.class));
    }

    public long getRefreshExpirationMs() {
        return refreshExpiryMs;
    }

    public Instant getExpiry(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().toInstant();
    }

}
