package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.entity.User;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.service.UsernameGenerationService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepo;
    private final UsernameGenerationService usernameService;

    public CustomOAuth2UserService(UserRepository userRepo,
                                   UsernameGenerationService usernameService) {
        this.userRepo = userRepo;
        this.usernameService = usernameService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // delegate to the default service to fetch user info from provider
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        // registrationId identifies provider e.g. "google" or "github"
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // key used by provider as the user name attribute (e.g. "sub" for Google, "id" for GitHub)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        // attributes returned by provider
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Extract email and name in a provider-agnostic way (providers differ)
        String email = extractEmail(registrationId, attributes);
        String name = extractName(registrationId, attributes);

        if (email == null || email.isBlank()) {
            // Provider did not return an email — treat as error for now
            throw new OAuth2AuthenticationException("OAuth2 provider did not return an email address");
        }

        // find existing user by email
        Optional<User> maybe = userRepo.findByEmail(email);

        User user;
        if (maybe.isPresent()) {
            user = maybe.get();
            // Optionally update user's display name if changed
            if (name != null && !name.isBlank() && !name.equals(user.getFullName())) {
                user.setFullName(name);
                userRepo.save(user);
            }
            // Could also record provider id/linking here
        } else {
            // Create local user: generate a safe unique username
            String base = suggestBaseUsername(name, email);
            String username = usernameService.generateAvailableUsername(base);

            // Create a new user (password null - social login)
            user = new User();
            // If your User constructor requires specific fields, adapt accordingly:
            user.setId(UUID.randomUUID());
            user.setFullName(name != null ? name : email);
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(null); // no password for social login
            user.setCreatedAt(java.time.Instant.now());

            userRepo.save(user);
        }

        // Create authorities (simple ROLE_USER)
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // return OAuth2User with the provider attributes and identifiers
        String nameAttributeKey = userNameAttributeName != null ? userNameAttributeName : "id";

        // Merge some local info into attributes if you like
        Map<String, Object> mappedAttrs = new HashMap<>(attributes);
        mappedAttrs.put("localUserId", user.getId().toString());
        mappedAttrs.put("localUsername", user.getUsername());
        mappedAttrs.put("localFullName", user.getFullName());

        return new DefaultOAuth2User(authorities, mappedAttrs, nameAttributeKey);
    }

    // Heuristic: extract email depending on provider; extend as needed
    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        // Google: "email"
        // GitHub: may not include email in attributes; GitHub returns emails via a separate endpoint unless configured
        if ("google".equalsIgnoreCase(registrationId)) {
            return (String) attributes.get("email");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            // If GitHub returns "email" directly, use it; otherwise you may need to call /user/emails endpoint.
            Object email = attributes.get("email");
            if (email instanceof String) return (String) email;
            // fallback: try "login" + "@users.noreply.github.com" (not ideal)
            Object login = attributes.get("login");
            if (login instanceof String) return login + "@users.noreply.github.com";
            return null;
        } else {
            // generic attempt
            Object e = attributes.get("email");
            return e == null ? null : e.toString();
        }
    }

    private String extractName(String registrationId, Map<String, Object> attributes) {
        // Typical attribute keys:
        // Google: "name"
        // GitHub: "name" or "login"
        Object name = attributes.get("name");
        if (name instanceof String && !((String) name).isBlank()) {
            return (String) name;
        }
        Object login = attributes.get("login");
        if (login instanceof String) return (String) login;
        return null;
    }

    // Create a base username suggestion from name or email
    private String suggestBaseUsername(String name, String email) {
        if (name != null && !name.isBlank()) {
            // keep letters, numbers, ., _
            String base = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._]", "");
            if (base.length() >= 3) return base;
        }
        // fallback to email local-part
        if (email != null && !email.isBlank()) {
            String local = email.split("@")[0].toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._]", "");
            if (local.length() >= 1) return local;
        }
        // ultimate fallback
        return "user";
    }
}
