package com.codeastras.backend.codeastras.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import  org.springframework.security.core.AuthenticationException;
import java.io.IOException;
import java.util.Map;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public  void  commence(HttpServletRequest request,
                           HttpServletResponse response,
                           AuthenticationException authException
                           ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), Map.of(
                "error","unauthorized","message",authException.getMessage()
        ));

    }
}
