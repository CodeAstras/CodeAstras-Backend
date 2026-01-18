package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.UUID;

@Service
public final class AuthUtil {

    private static final Logger LOG =
            LoggerFactory.getLogger(AuthUtil.class);

    private AuthUtil() {}

    // HTTP / REST AUTH
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return requireUserId(auth);
    }

    public static UUID requireUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new UnauthorizedException("No authentication");
        }

        Object principal = auth.getPrincipal();

        try {
            if (principal instanceof UUID id) {
                return id;
            }

            if (principal instanceof String s) {
                return UUID.fromString(s);
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid principal value: {}", principal);
            throw new UnauthorizedException("Invalid user identity");
        }

        LOG.warn("Unsupported principal type: {}", principal.getClass());
        throw new UnauthorizedException("Invalid principal type");
    }

    // WEBSOCKET AUTH

    public static UUID requireUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new UnauthorizedException("No principal");
        }

        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid WS principal name: {}", principal.getName());
            throw new UnauthorizedException("Invalid user identity");
        }
    }
}
