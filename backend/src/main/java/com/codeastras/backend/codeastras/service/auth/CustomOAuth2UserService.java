package com.codeastras.backend.codeastras.service.auth;

import com.codeastras.backend.codeastras.entity.auth.User;
import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.service.UsernameGenerationService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    private final UserRepository userRepo;
    private final UsernameGenerationService usernameService;

    public CustomOAuth2UserService(
            UserRepository userRepo,
            UsernameGenerationService usernameService
    ) {
        this.userRepo = userRepo;
        this.usernameService = usernameService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String provider =
                userRequest.getClientRegistration().getRegistrationId();

        String providerUserId =
                extractProviderUserId(userRequest, oauth2User);

        Map<String, Object> attributes =
                new HashMap<>(oauth2User.getAttributes());

        String email = extractEmail(provider, attributes);
        String name = extractName(attributes);

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "OAuth2 provider did not return a usable email"
            );
        }


        // SINGLE LOCAL USER (email is the current trust anchor)
        User user = userRepo.findByEmail(email)
                .map(existing -> updateExistingUser(existing, name))
                .orElseGet(() -> createNewUser(email, name, provider));


        // Authorities (future-proof hook)
        Set<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Stable principal key (LOCAL user id)
        String principalKey = "localUserId";

        attributes.put(principalKey, user.getId().toString());
        attributes.put("localUsername", user.getUsername());
        attributes.put("localFullName", user.getFullName());
        attributes.put("oauthProvider", provider);
        attributes.put("oauthProviderUserId", providerUserId);

        return new DefaultOAuth2User(
                authorities,
                attributes,
                principalKey
        );
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private User updateExistingUser(User user, String name) {
        if (name != null && !name.isBlank()
                && !name.equals(user.getFullName())) {
            user.setFullName(name);
            userRepo.save(user);
        }
        return user;
    }

    private User createNewUser(String email, String name, String provider) {

        String base = suggestBaseUsername(name, email);
        String username = usernameService.generateAvailableUsername(base);

        // OAuth users do NOT log in via password
        // Use a random, unguessable value
        String randomSecret = UUID.randomUUID().toString();

        User user = new User(
                name != null ? name : email,
                username,
                email,
                randomSecret,   // will never be used
                provider        // GITHUB / GOOGLE
        );

        return userRepo.save(user);
    }


    private String extractProviderUserId(
            OAuth2UserRequest request,
            OAuth2User user
    ) {
        String key = request.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Object value = user.getAttributes().get(key);
        return value == null ? null : value.toString();
    }

    private String extractEmail(
            String provider,
            Map<String, Object> attributes
    ) {
        Object email = attributes.get("email");
        if (email instanceof String && !((String) email).isBlank()) {
            return (String) email;
        }

        // GitHub fallback — explicit, visible, intentional
        if ("github".equalsIgnoreCase(provider)) {
            Object login = attributes.get("login");
            if (login instanceof String) {
                return login + "@users.noreply.github.com";
            }
        }
        return null;
    }

    private String extractName(Map<String, Object> attributes) {
        Object name = attributes.get("name");
        if (name instanceof String && !((String) name).isBlank()) {
            return (String) name;
        }
        Object login = attributes.get("login");
        if (login instanceof String) return (String) login;
        return null;
    }

    private String suggestBaseUsername(String name, String email) {
        if (name != null && !name.isBlank()) {
            String base = name.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9._]", "");
            if (base.length() >= 3) return base;
        }
        if (email != null && !email.isBlank()) {
            String local = email.split("@")[0]
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9._]", "");
            if (!local.isBlank()) return local;
        }
        return "user";
    }
}
