package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepo; // kept for future hard checks if needed

    public JwtAuthenticationFilter(
            JwtUtils jwtUtils,
            UserRepository userRepo
    ) {
        this.jwtUtils = jwtUtils;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String header = resolveTokenFromRequest(request);

            if (header != null && header.startsWith("Bearer ")) {

                String token = header.substring(7);

                jwtUtils.validateAccessToken(token); // 🔒 STRICT

                UUID userId = jwtUtils.getUserId(token);

                var authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_USER"));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId.toString(),   // principal
                                null,
                                authorities
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            request.setAttribute("jwt_error", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }


    // ================= HELPERS =================

    private boolean isPublicPath(String path) {
        return path.startsWith("/oauth2")
                || path.startsWith("/login/oauth2")
                || path.startsWith("/api/auth")
                || path.startsWith("/api/health")
                || path.equals("/")
                || path.startsWith("/error")
                || path.startsWith("/ws");
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        if (header != null && !header.isBlank()) {
            return header;
        }

        // Optional legacy support (NOT recommended for prod)
        String param = request.getParameter("access_token");
        if (param != null && !param.isBlank()) {
            return "Bearer " + param;
        }

        return null;
    }
}
