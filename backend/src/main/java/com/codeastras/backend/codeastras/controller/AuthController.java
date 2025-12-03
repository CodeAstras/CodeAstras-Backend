package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.LoginRequest;
import com.codeastras.backend.codeastras.dto.SignupRequest;
import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.service.AuthService;
import com.codeastras.backend.codeastras.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;
    private final JwtUtils jwt;

    public AuthController(AuthService authService,
                          UserRepository userRepo,
                          JwtUtils jwt) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.jwt = jwt;
    }

    // SIGNUP -> creates user, issues access token + refresh cookie
    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody SignupRequest req,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        // create user and return access token
        String access = authService.signup(req);

        // fetch user to create refresh session (AuthService already has utilities)
        User user = userRepo.findByEmail(req.getEmail().toLowerCase()).orElseThrow();

        var gen = authService.createRefreshForUser(user, request.getHeader("User-Agent"), request.getRemoteAddr());

        // set httpOnly cookie for refresh token
        ResponseCookie cookie = ResponseCookie.from("refresh_token", gen.refreshJwt())
                .httpOnly(true)
                .secure(false) // set true in prod (https)
                .path("/")
                .maxAge(Duration.ofMillis(authService.getRefreshExpiryMs()))
                .sameSite("Lax")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return Map.of("accessToken", access);
    }

    // LOGIN -> verifies credentials, issues access token + refresh cookie
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest req,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        // validate credentials and get access token
        String access = authService.login(req);

        // fetch user entity to create refresh session
        var user = userRepo.findByEmail(req.getEmail().toLowerCase()).orElseThrow();

        var gen = authService.createRefreshForUser(user, request.getHeader("User-Agent"), request.getRemoteAddr());

        ResponseCookie cookie = ResponseCookie.from("refresh_token", gen.refreshJwt())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(authService.getRefreshExpiryMs()))
                .sameSite("Lax")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return Map.of("accessToken", access);
    }

    // ROTATE: refresh endpoint (cookie -> new access + rotate refresh)
    @PostMapping("/refresh")
    public Map<String,String> refresh(@CookieValue(name = "refresh_token", required = false) String refreshCookie,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (refreshCookie == null) throw new IllegalArgumentException("no refresh cookie");

        // verify and parse session id from refresh JWT (JwtUtils must expose method)
        String sessionId = jwt.getSessionId(refreshCookie);

        var rotated = authService.rotateRefresh(sessionId, request.getHeader("User-Agent"), request.getRemoteAddr());

        // set new refresh cookie (rotated)
        ResponseCookie cookie = ResponseCookie.from("refresh_token", rotated.refreshJwt())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(authService.getRefreshExpiryMs()))
                .sameSite("Lax")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return Map.of("accessToken", rotated.accessToken());
    }

    // LOGOUT endpoint -> revoke session + clear cookie
    @PostMapping("/logout")
    public Map<String, String> logout(@CookieValue(name = "refresh_token", required = false) String refreshCookie,
                                      HttpServletResponse response) {
        if (refreshCookie != null) {
            String sessionId = jwt.getSessionId(refreshCookie);
            authService.revokeSession(sessionId);
        }

        // clear cookie
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return Map.of("status", "ok");
    }
}
