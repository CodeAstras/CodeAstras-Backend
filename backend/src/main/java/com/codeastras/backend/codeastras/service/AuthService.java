package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.LoginRequest;
import com.codeastras.backend.codeastras.dto.SignupRequest;
import com.codeastras.backend.codeastras.entity.RefreshToken;
import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.repository.RefreshTokenRepository;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.security.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwt;
    private final UsernameGenerationService usernameService;
    private final RefreshTokenRepository refreshTokenRepo;

    public AuthService(
            UserRepository userRepo,
            PasswordEncoder passwordEncoder,
            JwtUtils jwt,
            UsernameGenerationService usernameService,
            RefreshTokenRepository refreshTokenRepo
    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.usernameService = usernameService;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    // ==================================================
    // SIGNUP
    // ==================================================
    @Transactional
    public String signup(SignupRequest req) {

        String email = req.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("email already in use");
        }

        String base = usernameService
                .suggestFromNameOrEmail(req.getFullName(), email);

        String username = usernameService.generateAvailableUsername(base);

        User user = userRepo.save(
                new User(
                        req.getFullName().trim(),
                        username,
                        email,
                        passwordEncoder.encode(req.getPassword())
                )
        );

        return jwt.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    // ==================================================
    // LOGIN
    // ==================================================
    public String login(LoginRequest req) {

        User user = userRepo.findByEmail(req.getEmail().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }

        return jwt.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    // ==================================================
    // CREATE REFRESH SESSION (IMMUTABLE)
    // ==================================================
    @Transactional
    public GeneratedRefresh createRefreshForUser(
            User user,
            String userAgent,
            String ip
    ) {

        String sessionId = UUID.randomUUID().toString();

        String refreshJwt =
                jwt.generateRefreshToken(user.getId(), sessionId);

        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID());
        rt.setUserId(user.getId());
        rt.setSessionId(sessionId);
        rt.setTokenHash(hash(refreshJwt));
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(
                Instant.now().plusMillis(jwt.getRefreshExpirationMs())
        );
        rt.setRevoked(false);
        rt.setUserAgent(userAgent);
        rt.setIp(ip);

        refreshTokenRepo.save(rt);

        return new GeneratedRefresh(sessionId, refreshJwt);
    }

    public record GeneratedRefresh(String sessionId, String refreshJwt) {}

    // ==================================================
    // ROTATE REFRESH TOKEN (SAFE)
    // ==================================================
    @Transactional
    public RotatedTokens rotateRefresh(
            String oldSessionId,
            String userAgent,
            String ip
    ) {

        RefreshToken old =
                refreshTokenRepo
                        .findBySessionIdAndRevokedFalse(oldSessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("invalid refresh session"));

        if (old.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("refresh token expired");
        }

        // 🔥 SAFE: revoke via UPDATE QUERY
        refreshTokenRepo.revokeBySessionId(oldSessionId);

        User user = userRepo.findById(old.getUserId())
                .orElseThrow(() -> new IllegalStateException("user not found"));

        GeneratedRefresh gen =
                createRefreshForUser(user, userAgent, ip);

        String newAccess =
                jwt.generateAccessToken(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                );

        return new RotatedTokens(newAccess, gen.refreshJwt());
    }

    public record RotatedTokens(String accessToken, String refreshJwt) {}

    // ==================================================
    // LOGOUT
    // ==================================================
    @Transactional
    public void revokeSession(String sessionId) {
        refreshTokenRepo.revokeBySessionId(sessionId);
    }

    // ==================================================
    // HASHING
    // ==================================================
    private String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(
                    md.digest(token.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash refresh token", e);
        }
    }

}
