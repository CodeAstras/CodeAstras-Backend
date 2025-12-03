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

    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwt,
                       UsernameGenerationService usernameService,
                       RefreshTokenRepository refreshTokenRepo) {

        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.usernameService = usernameService;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    // --------------------------
    // SIGNUP
    // --------------------------
    @Transactional
    public String signup(SignupRequest req) {
        String fullName = req.getFullName().trim();
        String email = req.getEmail().trim().toLowerCase(Locale.ROOT);
        String rawUsername = Optional.ofNullable(req.getUsername())
                .orElse("")
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("email already in use");
        }

        String base = rawUsername.isBlank() ?
                usernameService.suggestFromNameOrEmail(fullName, email) :
                rawUsername;

        base = base.replaceAll("[^a-z0-9._]", "");
        if (base.length() < 3) base += "01";

        String username = usernameService.generateAvailableUsername(base);

        String hashed = passwordEncoder.encode(req.getPassword());

        User saved = userRepo.save(
                new User(fullName, username, email, hashed)
        );

        return jwt.generateAccessToken(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    // --------------------------
    // LOGIN
    // --------------------------
    public String login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }

        return jwt.generateAccessToken(user.getId(), user.getUsername(), user.getEmail());
    }

    // --------------------------
    // CREATE REFRESH SESSION
    // --------------------------
    @Transactional
    public GeneratedRefresh createRefreshForUser(User user, String userAgent, String ip) {
        UUID sessionId = UUID.randomUUID();

        // 1. Generate refresh JWT
        String refreshJwt = jwt.generateRefreshToken(user.getId(), sessionId.toString());

        // 2. Hash the JWT for DB storage
        String hash = sha256Base64(refreshJwt);

        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID());
        rt.setUserId(user.getId());
        rt.setSessionId(sessionId.toString());
        rt.setTokenHash(hash);
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plusMillis(getRefreshExpiryMs()));
        rt.setRevoked(false);
        rt.setUserAgent(userAgent);
        rt.setIp(ip);

        refreshTokenRepo.save(rt);

        return new GeneratedRefresh(sessionId.toString(), refreshJwt);
    }

    public record GeneratedRefresh(String sessionId, String refreshJwt) {}

    public long getRefreshExpiryMs() {
        return 7L * 24 * 60 * 60 * 1000;
    }

    // --------------------------
    // ROTATE REFRESH TOKEN
    // --------------------------
    @Transactional
    public RotatedTokens rotateRefresh(String oldSessionId, String userAgent, String ip) {

        RefreshToken old = refreshTokenRepo.findBySessionId(oldSessionId)
                .orElseThrow(() -> new IllegalArgumentException("invalid refresh session"));

        if (old.isRevoked() || old.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("refresh token expired or revoked");
        }

        old.setRevoked(true);
        refreshTokenRepo.save(old);

        User user = userRepo.findById(old.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        GeneratedRefresh gen = createRefreshForUser(user, userAgent, ip);

        String newAccess = jwt.generateAccessToken(user.getId(), user.getUsername(), user.getEmail());

        return new RotatedTokens(newAccess, gen.refreshJwt());
    }

    public record RotatedTokens(String accessToken, String refreshJwt) {}

    // --------------------------
    // LOGOUT
    // --------------------------
    @Transactional
    public void revokeSession(String sessionId) {
        refreshTokenRepo.findBySessionId(sessionId).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
        });
    }

    private String sha256Base64(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(
                    md.digest(input.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash refresh token", e);
        }
    }
}
