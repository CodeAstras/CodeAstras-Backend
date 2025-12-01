package com.codeastras.backend.codeastras.service;


import com.codeastras.backend.codeastras.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UsernameGenerationService {

    private final UserRepository userRepo;

    public UsernameGenerationService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public String generateAvailableUsername(String base) {
        // Only allow a–z, 0–9, dot, underscore
        String candidate = base.replaceAll("[^a-z0-9._]", "").toLowerCase();

        // minimum 3 chars
        if (candidate.length() < 3) {
            candidate = candidate + "01";
        }

        String original = candidate;
        int i = 0;

        // add suffix until a unique username is found
        while (userRepo.existsByUsername(candidate)) {
            i++;
            candidate = original + i;
        }

        return candidate;
    }
}

