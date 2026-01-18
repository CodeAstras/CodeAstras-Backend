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

    // MESSAGE BROKER
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler te = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        te.setPoolSize(1);
        te.setThreadNamePrefix("wss-heartbeat-thread-");
        te.initialize();

        // Simple in-memory broker (OK for now)
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] { 10000, 10000 }) // Heartbeat every 10s
                .setTaskScheduler(te);

        // All client SEND must go through /app
        config.setApplicationDestinationPrefixes("/app");
    }

    // STOMP ENDPOINT
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                // 🔐 Restrict origins (match your frontend)
                .setAllowedOriginPatterns(
                        "http://localhost:3000")
                // Enable SockJS fallback
                .withSockJS();
    }

    // INBOUND SECURITY
    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {
        registration.interceptors(permissionInterceptor);
    }

    @org.springframework.web.socket.config.annotation.EnableWebSocket
    @Configuration
    static class RawWebSocketConfig implements org.springframework.web.socket.config.annotation.WebSocketConfigurer {

        private final com.codeastras.backend.codeastras.websocket.chat.ChatWebSocketHandler chatHandler;
        private final com.codeastras.backend.codeastras.websocket.chat.ChatHandshakeInterceptor chatInterceptor;

        RawWebSocketConfig(com.codeastras.backend.codeastras.websocket.chat.ChatWebSocketHandler chatHandler,
                com.codeastras.backend.codeastras.websocket.chat.ChatHandshakeInterceptor chatInterceptor) {
            this.chatHandler = chatHandler;
            this.chatInterceptor = chatInterceptor;
        }

        @Override
        public void registerWebSocketHandlers(
                org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry registry) {
            registry.addHandler(chatHandler, "/ws/chat/{projectId}")
                    .addInterceptors(chatInterceptor)
                    .setAllowedOriginPatterns("*"); // Allow all for now, specific origins better in prod
        }
    }
}
