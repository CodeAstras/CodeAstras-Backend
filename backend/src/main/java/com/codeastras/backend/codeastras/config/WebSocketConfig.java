package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.security.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final RoomWebSocketHandler roomWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(RoomWebSocketHandler roomWebSocketHandler, RoomWebSocketHandler roomWebScoketHandler,
                           JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.roomWebSocketHandler = roomWebSocketHandler;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }


    // STOMP over WebSocket (for app events)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // simple broker for topics; can be replaced by RabbitMQ/Redis when scaling
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Frontend connects here for STOMP (chat/events)
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")   // restrict in prod
                .withSockJS();
    }

    // Register raw WebSocket handler for signaling
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // client connects to /ws/signal/{roomId}
        registry.addHandler((WebSocketHandler) roomWebSocketHandler, "/ws/signal/*")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*"); // restrict in prod
    }

}
