package com.codeastras.backend.codeastras.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class CookieFactory {

    private CookieFactory() {}

    public static ResponseCookie refreshToken(String token, long maxAgeMs) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(false) // 🔥 true in prod (https)
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMs))
                .sameSite("Lax") // 🔥 None + Secure in prod
                .build();
    }

    public static ResponseCookie clearRefreshToken() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
