package com.ecommerce.websocket;

import com.ecommerce.dto.ChatMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketChatService {

    private final ObjectMapper objectMapper;

    // Store sessions by user ID
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    // Store sessions by conversation ID for targeted broadcasting
    private final Map<Long, Set<WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();

    public void registerSession(Long userId, Long conversationId, WebSocketSession session) {
        log.info("Registering WebSocket session for user {} in conversation {}", userId, conversationId);

        // Register by user ID
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);

        // Register by conversation ID
        if (conversationId != null) {
            conversationSessions.computeIfAbsent(conversationId, k -> new CopyOnWriteArraySet<>()).add(session);
        }
    }

    public void unregisterSession(Long userId, Long conversationId, WebSocketSession session) {
        log.info("Unregistering WebSocket session for user {} in conversation {}", userId, conversationId);

        // Unregister from user sessions
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }

        // Unregister from conversation sessions
        if (conversationId != null) {
            Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    conversationSessions.remove(conversationId);
                }
            }
        }
    }

    public void broadcastMessage(Long conversationId, ChatMessageDTO message) {
        log.debug("Broadcasting message to conversation {}", conversationId);

        Map<String, Object> wsMessage = Map.of(
                "type", "NEW_MESSAGE",
                "conversationId", conversationId,
                "message", message);

        sendToConversation(conversationId, wsMessage);
    }

    public void notifyTyping(Long conversationId, Long userId, String userName, boolean isTyping) {
        log.debug("User {} is {} in conversation {}", userName, isTyping ? "typing" : "stopped typing", conversationId);

        Map<String, Object> wsMessage = Map.of(
                "type", "TYPING",
                "conversationId", conversationId,
                "userId", userId,
                "userName", userName,
                "isTyping", isTyping);

        sendToConversation(conversationId, wsMessage);
    }

    public void notifyAiTyping(Long conversationId, boolean isTyping) {
        log.debug("AI is {} in conversation {}", isTyping ? "typing" : "stopped typing", conversationId);

        Map<String, Object> wsMessage = Map.of(
                "type", "AI_TYPING",
                "conversationId", conversationId,
                "isTyping", isTyping);

        sendToConversation(conversationId, wsMessage);
    }

    public void notifyMessagesRead(Long conversationId, Long userId) {
        log.debug("Messages read by user {} in conversation {}", userId, conversationId);

        Map<String, Object> wsMessage = Map.of(
                "type", "MESSAGES_READ",
                "conversationId", conversationId,
                "userId", userId);

        sendToConversation(conversationId, wsMessage);
    }

    public void notifyNewConversation(Long conversationId) {
        log.debug("New conversation created: {}", conversationId);

        Map<String, Object> wsMessage = Map.of(
                "type", "NEW_CONVERSATION",
                "conversationId", conversationId);

        // Broadcast to all admin sessions
        sendToAllAdmins(wsMessage);
    }

    public void notifyConversationUpdate(Long conversationId) {
        log.debug("Conversation updated: {}", conversationId);

        Map<String, Object> wsMessage = Map.of(
                "type", "CONVERSATION_UPDATE",
                "conversationId", conversationId);

        sendToConversation(conversationId, wsMessage);
    }

    public void sendToUser(Long userId, Map<String, Object> message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            String messageJson = serializeMessage(message);
            if (messageJson != null) {
                sessions.forEach(session -> sendMessage(session, messageJson));
            }
        }
    }

    private void sendToConversation(Long conversationId, Map<String, Object> message) {
        Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions != null && !sessions.isEmpty()) {
            String messageJson = serializeMessage(message);
            if (messageJson != null) {
                sessions.forEach(session -> sendMessage(session, messageJson));
            }
        }
    }

    private void sendToAllAdmins(Map<String, Object> message) {
        // For now, broadcast to all sessions
        // In production, filter by admin role
        String messageJson = serializeMessage(message);
        if (messageJson != null) {
            conversationSessions.values()
                    .forEach(sessions -> sessions.forEach(session -> sendMessage(session, messageJson)));
        }
    }

    private void sendMessage(WebSocketSession session, String message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Error sending WebSocket message to session {}", session.getId(), e);
            }
        }
    }

    private String serializeMessage(Map<String, Object> message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing WebSocket message", e);
            return null;
        }
    }

    public int getActiveSessionsCount() {
        return userSessions.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    public int getConversationSessionsCount(Long conversationId) {
        Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
        return sessions != null ? sessions.size() : 0;
    }
}
