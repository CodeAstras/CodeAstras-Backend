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
    private final long jwtExpirationMs;

    public JwtUtils(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes());
        this.jwtExpirationMs = props.getExpirationMs();
    }

    public String generateToken(UUID userId, String username, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            // log if needed
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return UUID.fromString(claims.getSubject());
    }
}
