package com.codeastras.backend.codeastras.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
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

    // --------------------
    // ACCESS TOKEN
    // --------------------
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

    // --------------------
    // REFRESH TOKEN
    // --------------------
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

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public UUID getUserId(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return UUID.fromString(c.getSubject());
    }

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

    // --------------------
    // NEEDED BY AuthService + OAuth2SuccessHandler
    // --------------------
    public long getRefreshExpirationMs() {
        return refreshExpiryMs;
    }
}
