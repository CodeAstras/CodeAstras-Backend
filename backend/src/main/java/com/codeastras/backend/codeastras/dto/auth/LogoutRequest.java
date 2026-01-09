package com.codeastras.backend.codeastras.dto.auth;

public class LogoutRequest {

    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}