package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.LoginRequest;
import com.codeastras.backend.codeastras.dto.SignupRequest;
import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.security.CookieFactory;
import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;
    private final JwtUtils jwt;

    public AuthController(
            AuthService authService,
            UserRepository userRepo,
            JwtUtils jwt
    ) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.jwt = jwt;
    }

    // ==================================================
    // SIGNUP
    // ==================================================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody SignupRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String accessToken = authService.signup(req);

            User user = userRepo.findByEmail(req.getEmail().toLowerCase())
                    .orElseThrow();

            var refresh = authService.createRefreshForUser(
                    user,
                    request.getHeader("User-Agent"),
                    request.getRemoteAddr()
            );

            response.setHeader(
                    HttpHeaders.SET_COOKIE,
                    CookieFactory
                            .refreshToken(
                                    refresh.refreshJwt(),
                                    jwt.getRefreshExpiryMs()
                            )
                            .toString()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("accessToken", accessToken));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "email_already_exists",
                            "message", e.getMessage()
                    ));
        }
    }

    // ==================================================
    // LOGIN
    // ==================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String accessToken = authService.login(req);

            User user = userRepo.findByEmail(req.getEmail().toLowerCase())
                    .orElseThrow();

            var refresh = authService.createRefreshForUser(
                    user,
                    request.getHeader("User-Agent"),
                    request.getRemoteAddr()
            );

            response.setHeader(
                    HttpHeaders.SET_COOKIE,
                    CookieFactory
                            .refreshToken(
                                    refresh.refreshJwt(),
                                    jwt.getRefreshExpiryMs()
                            )
                            .toString()
            );

            return ResponseEntity.ok(
                    Map.of("accessToken", accessToken)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "invalid_credentials",
                            "message", "Invalid email or password"
                    ));
        }
    }

    // ==================================================
    // REFRESH TOKEN ROTATION
    // ==================================================
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (refreshCookie == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "missing_refresh_token"));
        }

        try {
            String sessionId = jwt.getSessionId(refreshCookie);

            var rotated = authService.rotateRefresh(
                    sessionId,
                    request.getHeader("User-Agent"),
                    request.getRemoteAddr()
            );

            response.setHeader(
                    HttpHeaders.SET_COOKIE,
                    CookieFactory
                            .refreshToken(
                                    rotated.refreshJwt(),
                                    jwt.getRefreshExpiryMs()
                            )
                            .toString()
            );

            return ResponseEntity.ok(
                    Map.of("accessToken", rotated.accessToken())
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "invalid_refresh_token",
                            "message", e.getMessage()
                    ));
        }
    }

    // ==================================================
    // LOGOUT
    // ==================================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            HttpServletResponse response
    ) {
        if (refreshCookie != null) {
            try {
                String sessionId = jwt.getSessionId(refreshCookie);
                authService.revokeSession(sessionId);
            } catch (Exception ignored) {
                // already invalid → safe to ignore
            }
        }

        response.setHeader(
                HttpHeaders.SET_COOKIE,
                CookieFactory.clearRefreshToken().toString()
        );

        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
