package com.ecommerce.websocket;

import com.ecommerce.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    // Store active sessions
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(session.getId(), session);
            sessionUserMap.put(session.getId(), userId);
            System.out.println("WebSocket connection established for user: " + userId);
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication required"));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String userId = sessionUserMap.get(session.getId());
        if (userId == null) {
            return;
        }

        try {
            String payload = message.getPayload().toString();
            Map<String, Object> chatMessage = objectMapper.readValue(payload, Map.class);
            
            // Process the message (save to database, broadcast to other users, etc.)
            processChatMessage(userId, chatMessage);
            
            // Echo back confirmation
            Map<String, Object> response = Map.of(
                "type", "MESSAGE_SENT",
                "messageId", chatMessage.get("id"),
                "status", "success"
            );
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            
        } catch (Exception e) {
            sendErrorMessage(session, "Failed to process message: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error for session " + session.getId() + ": " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userId = sessionUserMap.remove(session.getId());
        sessions.remove(session.getId());
        System.out.println("WebSocket connection closed for user: " + userId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String getUserIdFromSession(WebSocketSession session) {
        // Extract user ID from session attributes
        // This would be set by the WebSocketAuthInterceptor
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }

    private void processChatMessage(String userId, Map<String, Object> message) {
        // TODO: Implement message processing
        // 1. Save message to database
        // 2. Broadcast to other conversation participants
        // 3. Send push notifications if needed
        System.out.println("Processing message from user " + userId + ": " + message);
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "message", errorMessage
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            System.err.println("Failed to send error message: " + e.getMessage());
        }
    }

    public void broadcastToConversation(Long conversationId, Map<String, Object> message) {
        // TODO: Implement broadcasting to specific conversation participants
        // This method will be called by the chat service when a new message is sent
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(messageJson);
            
            // For now, broadcast to all connected sessions
            // In production, filter by conversation participants
            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to broadcast message: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to broadcast message: " + e.getMessage());
        }
    }
}