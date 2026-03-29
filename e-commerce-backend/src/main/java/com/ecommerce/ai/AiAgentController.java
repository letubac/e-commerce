package com.ecommerce.ai;

import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.webapp.BusinessApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller cho AI Support Agent.
 * <p>
 * Endpoint trực tiếp để kiểm thử AI agent mà không qua chat WebSocket.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAgentController {

    private final AiSupportService aiSupportService;
    private final ErrorHandler errorHandler;
    private final SuccessHandler successHandler;

    /**
     * Gửi tin nhắn đến AI và nhận phản hồi trực tiếp (không qua DB).
     * Dùng để test AI agent hoặc tích hợp với chat flow.
     */
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BusinessApiResponse> chat(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String message = (String) body.get("message");
            Object convIdObj = body.get("conversationId");
            Long conversationId = 0L;
            if (convIdObj != null) {
                try {
                    conversationId = Long.parseLong(convIdObj.toString());
                } catch (NumberFormatException e) {
                    return ResponseEntity.ok(errorHandler.handlerException(
                            new IllegalArgumentException("conversationId phải là số nguyên hợp lệ"), start));
                }
            }

            if (message == null || message.isBlank()) {
                return ResponseEntity.ok(errorHandler.handlerException(
                        new IllegalArgumentException("message không được để trống"), start));
            }

            if (!aiSupportService.isEnabled()) {
                Map<String, Object> resp = Map.of(
                        "reply", "AI Assistant hiện chưa được kích hoạt. "
                                + "Vui lòng cấu hình OPENAI_API_KEY và bật app.ai.enabled=true.",
                        "aiEnabled", false);
                return ResponseEntity.ok(successHandler.handlerSuccess(resp, start));
            }

            String reply = aiSupportService.respond(conversationId, message);
            Map<String, Object> resp = Map.of(
                    "reply", reply != null ? reply : "Không có phản hồi từ AI.",
                    "aiEnabled", true,
                    "conversationId", conversationId);
            return ResponseEntity.ok(successHandler.handlerSuccess(resp, start));
        } catch (Exception e) {
            log.error("Error in AI chat endpoint", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Kiểm tra trạng thái AI agent.
     */
    @GetMapping("/status")
    public ResponseEntity<BusinessApiResponse> getStatus() {
        long start = System.currentTimeMillis();
        Map<String, Object> status = Map.of(
                "aiEnabled", aiSupportService.isEnabled(),
                "message", aiSupportService.isEnabled()
                        ? "AI Support Agent đang hoạt động."
                        : "AI Support Agent chưa được kích hoạt.");
        return ResponseEntity.ok(successHandler.handlerSuccess(status, start));
    }

    /**
     * Xóa bộ nhớ hội thoại của AI (admin only).
     */
    @DeleteMapping("/memory/{conversationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> clearMemory(
            @PathVariable Long conversationId) {
        long start = System.currentTimeMillis();
        aiSupportService.clearMemory(conversationId);
        return ResponseEntity.ok(successHandler.handlerSuccess(
                Map.of("message", "AI memory đã được xóa cho conversation " + conversationId), start));
    }
}
