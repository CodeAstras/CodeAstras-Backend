package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.config.OAuth2Config;
import com.codeastras.backend.codeastras.security.CookieFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.repository.RefreshTokenRepository;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.service.AuthService;
import com.codeastras.backend.codeastras.service.UsernameGenerationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final UsernameGenerationService usernameService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwt;
    private final OAuth2Config oauth2Config;
    private final AuthService authService;


    public OAuth2SuccessHandler(
            UserRepository userRepo,
            RefreshTokenRepository refreshRepo,
            UsernameGenerationService usernameService,
            PasswordEncoder passwordEncoder,
            JwtUtils jwt,
            OAuth2Config oauth2Config, AuthService authService
    ) {
        this.userRepo = userRepo;
        this.refreshRepo = refreshRepo;
        this.usernameService = usernameService;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.oauth2Config = oauth2Config;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication auth) throws IOException {

        SecurityContextHolder.clearContext();
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) auth;
        OAuth2User oauthUser = oauth.getPrincipal();

        String email = (String) oauthUser.getAttributes().get("email");

        if (email == null || email.isBlank()) {
            response.sendError(400, "OAuth provider did not supply email");
            return;
        }

        email = email.toLowerCase(Locale.ROOT);

        String finalEmail = email;
        User user = userRepo.findByEmail(email).orElseGet(() -> {
            String name = Optional.ofNullable((String) oauthUser.getAttributes().get("name"))
                    .orElse(finalEmail.split("@")[0]);

            String baseUsername = usernameService.suggestFromNameOrEmail(name, finalEmail);
            String username = usernameService.generateAvailableUsername(baseUsername);

            String randomPwd = UUID.randomUUID().toString();
            String hashed = passwordEncoder.encode(randomPwd);

            User u = new User(name, username, finalEmail, hashed);
            return userRepo.save(u);
        });

        // ---- Refresh Token & DB session (USE AUTH SERVICE TO AVOID HASHING MISMATCH) ----
        var gen = authService.createRefreshForUser(
                    user, 
                    request.getHeader("User-Agent"), 
                    request.getRemoteAddr()
            );
        String refreshJwt = gen.refreshJwt();

        // ---- Access Token ----
        String access = jwt.generateAccessToken(user.getId(), user.getUsername(), user.getEmail());

        // ---- Cookie ----
        ResponseCookie cookie = CookieFactory.refreshToken(refreshJwt, jwt.getRefreshExpirationMs());

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ---- Redirect to FRONTEND ----
        String redirectUrl = oauth2Config.getFrontendBaseUrl()
                + "/oauth-success?access="
                + URLEncoder.encode(access, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(
                    digest.digest(input.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
