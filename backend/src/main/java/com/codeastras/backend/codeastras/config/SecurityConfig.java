package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.repository.UserRepository;
import com.codeastras.backend.codeastras.security.*;
import com.codeastras.backend.codeastras.service.auth.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtUtils jwt;
    private final UserRepository userRepository;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(
            JwtUtils jwt,
            UserRepository userRepository,
            CustomOAuth2UserService customOAuth2UserService
    ) {
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    // ==================================================
    // OAuth2 STATE (COOKIE-BASED)
    // ==================================================

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository
    oAuth2AuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    // ==================================================
    // SECURITY FILTER CHAIN
    // ==================================================

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2SuccessHandler oauth2SuccessHandler,
            OAuth2FailureHandler oauth2FailureHandler
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwt, userRepository);

        http
                // ---------------- CORS ----------------
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cfg = new CorsConfiguration();

                    cfg.setAllowedOriginPatterns(List.of(
                            "http://localhost:3000"
                    ));
                    cfg.setAllowedMethods(List.of(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                    ));
                    cfg.setAllowedHeaders(List.of(
                            "Authorization",
                            "Content-Type",
                            "X-Requested-With"
                    ));
                    cfg.setExposedHeaders(List.of("Set-Cookie"));
                    cfg.setAllowCredentials(true);

                    return cfg;
                }))

                // ---------------- CSRF / SESSION ----------------
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ---------------- AUTHZ ----------------
                .authorizeHttpRequests(auth -> auth

                        // Public auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // OAuth2 endpoints
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()

                        // Health
                        .requestMatchers("/api/health").permitAll()

                        // WebSocket handshake ONLY
                        .requestMatchers(
                                "/ws",
                                "/ws/**",
                                "/ws/info",
                                "/ws/info/**"
                        ).permitAll()

                        // Everything else
                        .anyRequest().authenticated()
                )

                // ---------------- EXCEPTIONS ----------------
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                new RestAuthenticationEntryPoint()
                        )
                )

                // ---------------- OAUTH2 LOGIN ----------------
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(a -> a
                                .authorizationRequestRepository(
                                        oAuth2AuthorizationRequestRepository()
                                )
                        )
                        .userInfoEndpoint(user ->
                                user.userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                )

                // ---------------- JWT FILTER ----------------
                // Must come AFTER OAuth2 processing
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ==================================================
    // AUTH BEANS
    // ==================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration cfg
    ) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
