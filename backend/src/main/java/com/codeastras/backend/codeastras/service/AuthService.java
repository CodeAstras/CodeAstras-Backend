package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.LoginRequest;
import com.codeastras.backend.codeastras.dto.SignupRequest;
import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.config.JwtTokenProvider;
import com.codeastras.backend.codeastras.security.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }
@Transactional
    public String signup(SignupRequest req) {
        String email = req.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = req.getUsername().trim();
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("email already in use");
        }
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use");
        }
        String hashed = passwordEncoder.encode(req.getPassword());
        User user = new User(
                req.getFullName(),
                username,
                email,
                hashed
        );

         User saved = userRepo.save(user);
        String token = jwtUtils.generateToken(saved.getId(), saved.getUsername(), saved.getEmail());
        return token;
    }

    public String login(LoginRequest req) {
        Optional<User> maybe = userRepo.findByEmail(req.getEmail());
        if (maybe.isEmpty()) {
            throw new IllegalArgumentException("invalid credentials");
        }
        User user = maybe.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        return jwtUtils.generateToken(
                user.getId(),
                user.getUsername(),
                user.getEmail());
    }
}
