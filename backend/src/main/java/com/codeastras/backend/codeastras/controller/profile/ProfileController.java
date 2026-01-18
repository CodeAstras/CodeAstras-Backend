package com.codeastras.backend.codeastras.controller.profile;

import com.codeastras.backend.codeastras.dto.profile.*;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.profile.ProfileCommandService;
import com.codeastras.backend.codeastras.service.profile.ProfileQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileQueryService profileQueryService;
    private final ProfileCommandService profileCommandService;

    public ProfileController(
            ProfileQueryService profileQueryService,
            ProfileCommandService profileCommandService
    ) {
        this.profileQueryService = profileQueryService;
        this.profileCommandService = profileCommandService;
    }

    /**
     * Public profile by username
     * Accessible without authentication
     */
    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(
            @PathVariable String username
    ) {
        return ResponseEntity.ok(
                profileQueryService.getPublicProfile(username)
        );
    }

    /**
     * Private profile (self)
     * Requires authentication
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        UUID userId = AuthUtil.getCurrentUserId();
        return ResponseEntity.ok(
                profileQueryService.getMyProfile(userId)
        );
    }

    /**
     * Update display name, bio, location
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UUID userId = AuthUtil.getCurrentUserId();
        profileCommandService.updateProfile(userId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update username (guarded, policy-driven)
     */
    @PutMapping("/me/username")
    public ResponseEntity<Void> updateUsername(
            @Valid @RequestBody UpdateUsernameRequest request
    ) {
        UUID userId = AuthUtil.getCurrentUserId();
        profileCommandService.updateUsername(userId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update avatar reference
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<Void> updateAvatar(
            @Valid @RequestBody UpdateAvatarRequest request
    ) {
        UUID userId = AuthUtil.getCurrentUserId();
        profileCommandService.updateAvatar(userId, request);
        return ResponseEntity.noContent().build();
    }
}
