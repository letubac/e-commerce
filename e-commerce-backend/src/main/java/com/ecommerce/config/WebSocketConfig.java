package com.ecommerce.config;

import com.ecommerce.websocket.ChatWebSocketHandler;
import com.ecommerce.websocket.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketConfigurer, WebSocketMessageBrokerConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    // Standard WebSocket for Chat
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001")
                .withSockJS();
    }

    // STOMP WebSocket for Notifications
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for sending messages to clients
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages from client to server
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP endpoint for notifications
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}