package com.codeastras.backend.codeastras.dto.auth;

import com.codeastras.backend.codeastras.validation.NotBanned;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Size(min = 3,max = 31)
    @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9._]{1,30})$", message = "Username may contain letters, numbers, '.' and '_' and must be 3-31 characters")
    @NotBanned(message = "That username is reserved")
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // No-arg constructor required for Jackson
    public SignupRequest() {}

     public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public static class ProjectSession {

        private String projectId;
        private String language;
        private String createdAt;

        public ProjectSession(String projectId, String language, String createdAt) {
            this.projectId = projectId;
            this.language = language;
            this.createdAt = createdAt;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
