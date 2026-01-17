package com.codeastras.backend.codeastras.service.profile;

import com.codeastras.backend.codeastras.dto.profile.ProfileResponse;
import com.codeastras.backend.codeastras.dto.profile.PublicProfileResponse;
import com.codeastras.backend.codeastras.entity.auth.User;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileQueryService {

    private final UserRepository userRepository;

    public ProfileQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PublicProfileResponse getPublicProfile(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));

        return new  PublicProfileResponse(
                user.getUsername(),
                user.getFullName(),
                user.getBio(),
                user.getLocation(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }

    public ProfileResponse getMyProfile(UUID userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User Not Found")
        );

        return new  ProfileResponse(
                user.getUsername(),
                user.getFullName(),
                user.getBio(),
                user.getLocation(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getEmail()
        );
    }
}