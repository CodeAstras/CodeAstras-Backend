package com.codeastras.backend.codeastras.security;

import org.springframework.web.socket.server.HandshakeInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JwtHandShakerInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;


    public JwtHandShakerInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        var url  = request.getURI();
        var query = url.getQuery();
        String token = null;

        if(query != null && query.contains("token")){
            for(String kv : query.split("&")){
                if(kv.startsWith("token")){
                    token = kv.substring("token".length());
                    break;
                }
            }
        }

        if(token == null && request.getHeaders().containsHeader("Authorization")){
            var auth = request.getHeaders().getFirst("Authorization");
            if(auth != null && auth.startsWith("Bearer ")) token = auth.substring("Bearer ".length());
        }


        if(token == null) return false;

        var userId = jwtService.validateAndGetUserId(token); // throw or return null if invalid
        if (userId == null) return false;

        // store user id in handshake attributes for later use by handler
        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception ex) {}

}
