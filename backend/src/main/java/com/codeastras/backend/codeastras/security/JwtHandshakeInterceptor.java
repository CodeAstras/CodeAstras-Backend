package com.codeastras.backend.codeastras.security;

import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwt;

    public JwtHandshakeInterceptor(JwtUtils jwt) {
        this.jwt = jwt;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String token = extractToken(request);

        if (token == null) {
            return false;
        }

        if (!jwt.validate(token)) {
            return false;
        }

        var userId = jwt.getUserId(token);

        if (userId == null) {
            return false;
        }

        // attach userId to WebSocket session
        attributes.put("userId", userId);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception ex) {}

    // -----------------------------------------
    // EXTRACT TOKEN FROM QUERY PARAM OR HEADER
    // -----------------------------------------
    private String extractToken(ServerHttpRequest request) {
        var url = request.getURI();
        var query = url.getQuery();

        if (query != null && query.contains("token=")) {
            for (String kv : query.split("&")) {
                if (kv.startsWith("token=")) {
                    return kv.substring("token=".length());
                }
            }
        }

        var auth = request.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring("Bearer ".length());
        }

        return null;
    }
}
