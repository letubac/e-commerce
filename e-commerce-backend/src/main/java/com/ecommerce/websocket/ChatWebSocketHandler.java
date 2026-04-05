package com.ecommerce.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
/**
 * author: LeTuBac
 */
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebSocketChatService webSocketChatService;

    // Store active sessions with metadata
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    private static class SessionInfo {
        Long userId;
        Long conversationId;
        String username;
        WebSocketSession session;

        SessionInfo(Long userId, Long conversationId, String username, WebSocketSession session) {
            this.userId = userId;
            this.conversationId = conversationId;
            this.username = username;
            this.session = session;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        String username = getUsernameFromSession(session);
        Long conversationId = getConversationIdFromSession(session);

        if (userId != null) {
            SessionInfo sessionInfo = new SessionInfo(userId, conversationId, username, session);
            sessions.put(session.getId(), sessionInfo);

            // Register with WebSocket service
            webSocketChatService.registerSession(userId, conversationId, session);

            log.info("WebSocket connection established - User: {}, Conversation: {}, Session: {}",
                    username, conversationId, session.getId());

            // Send connection confirmation
            sendConnectionConfirmation(session, userId, username);
        } else {
            log.warn("WebSocket connection rejected - No valid authentication");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication required"));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        SessionInfo sessionInfo = sessions.get(session.getId());
        if (sessionInfo == null) {
            log.warn("Message received from unregistered session: {}", session.getId());
            return;
        }

        try {
            String payload = message.getPayload().toString();
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);

            String messageType = (String) messageData.get("type");

            switch (messageType != null ? messageType : "") {
                case "TYPING":
                    handleTypingIndicator(sessionInfo, messageData);
                    break;

                case "READ":
                    handleReadReceipt(sessionInfo, messageData);
                    break;

                case "PING":
                    handlePing(session);
                    break;

                case "MESSAGE":
                    // Messages should be sent via REST API, not WebSocket
                    sendError(session, "Please use REST API to send messages");
                    break;

                default:
                    log.warn("Unknown message type: {}", messageType);
                    sendError(session, "Unknown message type");
            }

        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
            sendError(session, "Failed to process message: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        SessionInfo sessionInfo = sessions.remove(session.getId());

        if (sessionInfo != null) {
            // Unregister from WebSocket service
            webSocketChatService.unregisterSession(
                    sessionInfo.userId,
                    sessionInfo.conversationId,
                    session);

            log.info("WebSocket connection closed - User: {}, Reason: {}",
                    sessionInfo.username, closeStatus.getReason());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // Helper methods

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr instanceof String) {
            try {
                return Long.parseLong((String) userIdAttr);
            } catch (NumberFormatException e) {
                log.error("Invalid userId format: {}", userIdAttr);
            }
        }
        return userIdAttr != null ? Long.parseLong(userIdAttr.toString()) : null;
    }

    private String getUsernameFromSession(WebSocketSession session) {
        Object username = session.getAttributes().get("username");
        return username != null ? username.toString() : "Unknown";
    }

    private Long getConversationIdFromSession(WebSocketSession session) {
        Object conversationIdAttr = session.getAttributes().get("conversationId");
        if (conversationIdAttr instanceof String) {
            try {
                return Long.parseLong((String) conversationIdAttr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return conversationIdAttr != null ? Long.parseLong(conversationIdAttr.toString()) : null;
    }

    private void handleTypingIndicator(SessionInfo sessionInfo, Map<String, Object> messageData) {
        Boolean isTyping = (Boolean) messageData.get("isTyping");
        Long conversationId = sessionInfo.conversationId;

        if (conversationId != null && isTyping != null) {
            webSocketChatService.notifyTyping(
                    conversationId,
                    sessionInfo.userId,
                    sessionInfo.username,
                    isTyping);
        }
    }

    private void handleReadReceipt(SessionInfo sessionInfo, Map<String, Object> messageData) {
        Long conversationId = sessionInfo.conversationId;

        if (conversationId != null) {
            webSocketChatService.notifyMessagesRead(conversationId, sessionInfo.userId);
        }
    }

    private void handlePing(WebSocketSession session) {
        try {
            Map<String, Object> pong = Map.of(
                    "type", "PONG",
                    "timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
        } catch (IOException e) {
            log.error("Error sending PONG", e);
        }
    }

    private void sendConnectionConfirmation(WebSocketSession session, Long userId, String username) {
        try {
            Map<String, Object> confirmation = Map.of(
                    "type", "CONNECTION_ESTABLISHED",
                    "userId", userId,
                    "username", username,
                    "timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(confirmation)));
        } catch (IOException e) {
            log.error("Error sending connection confirmation", e);
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                    "type", "ERROR",
                    "message", errorMessage,
                    "timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            log.error("Failed to send error message", e);
        }
    }
}