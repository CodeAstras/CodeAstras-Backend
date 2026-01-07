package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private final Key key;
    private final long accessExpiryMs;

    @Getter
    private final long refreshExpiryMs;

    public JwtUtils(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes());
        this.accessExpiryMs = props.getAccessExpirationMs();
        this.refreshExpiryMs = props.getRefreshExpirationMs();
    }

    // ==================================================
    // TOKEN GENERATION
    // ==================================================

    public String generateAccessToken(UUID userId, String username, String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("type", "access")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessExpiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String sessionId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("sid", sessionId)
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshExpiryMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==================================================
    // INTERNAL PARSER (SINGLE SOURCE OF TRUTH)
    // ==================================================

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid or expired JWT");
        }
    }

    // ==================================================
    // 🔐 VALIDATION
    // ==================================================

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    public void validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!"access".equals(claims.get("type", String.class))) {
            throw new UnauthorizedException("Not an access token");
        }
    }

    public void validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new UnauthorizedException("Not a refresh token");
        }
    }

    // ==================================================
    // 👤 USER ID EXTRACTION
    // ==================================================

    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public UUID getUserIdFromToken(String token) {
        return getUserId(token);
    }

    // ==================================================
    // TOKEN TYPE HELPERS
    // ==================================================

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("type", String.class));
    }

    // ==================================================
    // MISC
    // ==================================================

    public String getSessionId(String refreshToken) {
        return parseClaims(refreshToken).get("sid", String.class);
    }

    public Instant getExpiry(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public long getRefreshExpirationMs() {
        return refreshExpiryMs;
    }
}
