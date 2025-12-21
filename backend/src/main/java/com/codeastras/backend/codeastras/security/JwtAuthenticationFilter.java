package com.codeastras.backend.codeastras.security;

import com.codeastras.backend.codeastras.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils tokenProvider;
    private final UserRepository userRepo;

    public JwtAuthenticationFilter(JwtUtils tokenProvider, UserRepository userRepo) {
        this.tokenProvider = tokenProvider;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // PUBLIC ENDPOINTS — JUST PASS THROUGH
        if (path.startsWith("/oauth2")
                || path.startsWith("/login/oauth2")
                || path.startsWith("/api/auth")
                || path.startsWith("/api/health")
                || path.equals("/")
                || path.startsWith("/error")) {

            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String header = resolveTokenFromRequest(request);

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (tokenProvider.validate(token)) {

                    //  refresh tokens must NOT authenticate APIs
                    if (!tokenProvider.isRefreshToken(token)) {

                        UUID userId = tokenProvider.getUserId(token);
                        userRepo.findById(userId).ifPresent(user -> {

                            var authorities =
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"));

                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userId,
                                            null,
                                            authorities
                                    );

                            SecurityContextHolder.getContext()
                                    .setAuthentication(auth);
                        });
                    }
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            request.setAttribute("jwt_error", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }



    private String resolveTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && !header.isBlank()) return header;
        // optional: support access token from query param for some flows (not recommended)
        String param = request.getParameter("access_token");
        if (param != null && !param.isBlank()) return "Bearer " + param;
        return null;
    }
}
