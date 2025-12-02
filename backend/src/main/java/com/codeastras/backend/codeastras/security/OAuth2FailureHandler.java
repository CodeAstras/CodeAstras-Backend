package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.config.OAuth2Config;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * On failure, redirect to frontend and include an error message.
 */
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final OAuth2Config oauth2Config;

    public OAuth2FailureHandler(OAuth2Config oauth2Config) {
        this.oauth2Config = oauth2Config;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String message = exception != null ? exception.getMessage() : "OAuth2 authentication failed";
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);

        String redirect = oauth2Config.getFrontendOauthSuccessPath() + "?error=" + encoded;
        response.sendRedirect(redirect);
    }
}
