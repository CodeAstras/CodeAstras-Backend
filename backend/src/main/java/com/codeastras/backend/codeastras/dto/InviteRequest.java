package com.codeastras.backend.codeastras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class InviteRequest {

    @NotBlank @Email
    private String email;

    private String message;

    public InviteRequest(String email, String message) {
        this.email = email;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
