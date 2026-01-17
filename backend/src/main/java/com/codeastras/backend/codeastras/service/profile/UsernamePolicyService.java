package com.codeastras.backend.codeastras.service.profile;

import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.exception.DuplicateResourceException;
import com.codeastras.backend.codeastras.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class UsernamePolicyService {

    private final UserRepository userRepository;

    public UsernamePolicyService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateUsernameChange(String newUsername, String currentUsername) {

        // No-op if same username
        if (newUsername.equalsIgnoreCase(currentUsername)) {
            throw new ValidationException("New username must be different");
        }

        // Uniqueness check
        if (userRepository.existsByUsername(newUsername)) {
            throw new DuplicateResourceException("Username already taken");
        }

        // Cooldown placeholder (future)
        // if (!cooldownPassed(userId)) { throw ... }

        // Reserved words, banned words, etc.
        // Hook into existing BannedWordValidator here later
    }
}

