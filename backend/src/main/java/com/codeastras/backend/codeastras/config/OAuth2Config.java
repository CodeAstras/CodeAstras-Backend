package com.codeastras.backend.codeastras.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuth2Config {

    private final String frontendBaseUrl;

    public OAuth2Config(
            @Value("${app.frontend.url:http://localhost:3000}")
            String frontendBaseUrl
    ) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    // 🔹 Base URL only (single source of truth)
    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    // 🔹 Semantic helpers (optional but clean)
    public String getFrontendOauthSuccessPath() {
        return frontendBaseUrl + "/oauth-success";
    }

    public String getFrontendOauthFailurePath() {
        return frontendBaseUrl + "/oauth-failure";
    }
}
