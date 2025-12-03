package com.codeastras.backend.codeastras.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Small config holder for OAuth2-related settings.
 * Expand this if you want to load more oauth2 properties from application.properties.
 */
@Component
public class OAuth2Config {

    /**
     * Frontend base URL where to redirect after OAuth success/failure
     * default is http://localhost:3000 for local dev.
     */
    private final String frontendBaseUrl;

    public OAuth2Config(@Value("${app.frontend.url:http://localhost:3000}") String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    /**
     * Convenience: frontend oauth success path
     */
    public String getFrontendOauthSuccessPath() {
        return frontendBaseUrl + "/oauth-success";
    }
}
