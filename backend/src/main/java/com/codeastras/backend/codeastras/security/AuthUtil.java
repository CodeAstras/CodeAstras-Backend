package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.UUID;

public final class AuthUtil {

    private AuthUtil() {}

    public static UUID requireUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new UnauthorizedException("No authentication");
        }

        Object p = auth.getPrincipal();

        if (p instanceof UUID id) return id;
        if (p instanceof String s) return UUID.fromString(s);

        throw new UnauthorizedException("Invalid principal type: " + p.getClass());
    }

    public static UUID requireUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new UnauthorizedException("No principal");
        }
        return UUID.fromString(principal.getName());
    }
}
