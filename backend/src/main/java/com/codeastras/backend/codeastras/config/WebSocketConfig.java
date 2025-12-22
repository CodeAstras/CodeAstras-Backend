package com.codeastras.backend.codeastras.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketPermissionInterceptor permissionInterceptor;

    public WebSocketConfig(WebSocketPermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    // ==================================================
    // MESSAGE BROKER
    // ==================================================

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // Simple in-memory broker (OK for now)
        config.enableSimpleBroker("/topic", "/queue");

        // All client SEND must go through /app
        config.setApplicationDestinationPrefixes("/app");
    }

    // ==================================================
    // STOMP ENDPOINT
    // ==================================================

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                // 🔐 Restrict origins (match your frontend)
                .setAllowedOriginPatterns(
                        "http://localhost:3000"
                )
                // Enable SockJS fallback
                .withSockJS();
    }

    // ==================================================
    // INBOUND SECURITY
    // ==================================================

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {
        registration.interceptors(permissionInterceptor);
    }
}
