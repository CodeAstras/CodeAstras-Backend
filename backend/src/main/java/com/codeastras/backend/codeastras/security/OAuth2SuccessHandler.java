package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.config.OAuth2Config;
import com.codeastras.backend.codeastras.config.JwtTokenProvider; // adjust import if your JwtTokenProvider is elsewhere
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a JWT for the authenticated local user and redirects to the frontend
 * with the token placed in the URL fragment (recommended for SPA).
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2Config oauth2Config;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider, OAuth2Config oauth2Config) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.oauth2Config = oauth2Config;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            // fallback: redirect to frontend login
            response.sendRedirect(oauth2Config.getFrontendOauthSuccessPath());
            return;
        }

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        // Attempt to read local user id saved by CustomOAuth2UserService
        String localUserId = getStringAttribute(attributes, "localUserId");
        String localUsername = getStringAttribute(attributes, "localUsername");
        String email = getStringAttribute(attributes, "email");
        if (email == null) {
            // maybe the attribute uses another key
            email = getStringAttribute(attributes, "localEmail");
        }

        String jwt;
        try {
            if (localUserId != null) {
                UUID userId = UUID.fromString(localUserId);
                jwt = jwtTokenProvider.generateToken(userId, localUsername, email);
            } else {
                // If we don't have a localUserId, try to generate token from other attributes
                // This branch assumes you can still create a token from username/email - adjust as needed
                // If JwtTokenProvider has a method generateToken(UUID) only, you'll need the user id.
                // Here we fallback to redirecting to frontend without token.
                response.sendRedirect(oauth2Config.getFrontendOauthSuccessPath());
                return;
            }
        } catch (Exception ex) {
            // Something went wrong creating the token - redirect without token
            response.sendRedirect(oauth2Config.getFrontendOauthSuccessPath());
            return;
        }

        // Put token in fragment to avoid it being sent to server in Referer header
        String redirect = oauth2Config.getFrontendOauthSuccessPath() + "#token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);

        response.sendRedirect(redirect);
    }

    private String getStringAttribute(Map<String, Object> attrs, String key) {
        Object v = attrs.get(key);
        if (v == null) return null;
        return v.toString();
    }
}
