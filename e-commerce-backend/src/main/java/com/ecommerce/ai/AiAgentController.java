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
 * REST Controller cho AI Agent Team (Phase 5).
 * <p>
 * Exposes endpoints for Support, Analytics, Inventory, Sales, Marketing, and Orchestrator agents.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class AiAgentController {

    private final AiSupportService aiSupportService;
    private final AdminAnalyticsAiService adminAnalyticsAiService;
    private final InventoryAgentService inventoryAgentService;
    private final SalesAgentService salesAgentService;
    private final MarketingAgentService marketingAgentService;
    private final OrchestratorAgentService orchestratorAgentService;
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

    /**
     * Ask the analytics AI a question about business data (Admin only).
     */
    @PostMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> analyzeData(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String question = (String) body.get("question");
            if (question == null || question.isBlank()) {
                return ResponseEntity.ok(errorHandler.handlerException(
                        new IllegalArgumentException("question must not be blank"), start));
            }

            String adminId = authentication.getName();

            if (!adminAnalyticsAiService.isEnabled()) {
                Map<String, Object> resp = Map.of(
                        "reply", "Analytics AI is not enabled. Configure OPENAI_API_KEY and set app.ai.enabled=true.",
                        "aiEnabled", false);
                return ResponseEntity.ok(successHandler.handlerSuccess(resp, start));
            }

            String reply = adminAnalyticsAiService.analyzeData(adminId, question);
            Map<String, Object> resp = Map.of(
                    "reply", reply != null ? reply : "No response from analytics AI.",
                    "aiEnabled", true);
            return ResponseEntity.ok(successHandler.handlerSuccess(resp, start));
        } catch (Exception e) {
            log.error("Error in analytics AI endpoint", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get the status of the analytics AI agent (Admin only).
     */
    @GetMapping("/analytics/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getAnalyticsStatus() {
        long start = System.currentTimeMillis();
        Map<String, Object> status = Map.of(
                "aiEnabled", adminAnalyticsAiService.isEnabled(),
                "message", adminAnalyticsAiService.isEnabled()
                        ? "Analytics AI Agent is active."
                        : "Analytics AI Agent is not enabled.");
        return ResponseEntity.ok(successHandler.handlerSuccess(status, start));
    }

    // ─────────────────────────────────────────────────────────────
    // Phase 5: Specialized agent endpoints
    // ─────────────────────────────────────────────────────────────

    /**
     * 📦 Inventory Agent - stock monitoring, low-stock alerts, restock suggestions (Admin only).
     */
    @PostMapping("/agents/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> inventoryAgent(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        return handleAgentRequest(body, authentication.getName(), "inventory",
                inventoryAgentService.isEnabled(),
                (adminId, q) -> inventoryAgentService.chat(adminId, q));
    }

    /**
     * 🛒 Sales Agent - revenue trends, top products, order analytics (Admin only).
     */
    @PostMapping("/agents/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> salesAgent(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        return handleAgentRequest(body, authentication.getName(), "sales",
                salesAgentService.isEnabled(),
                (adminId, q) -> salesAgentService.chat(adminId, q));
    }

    /**
     * 🎯 Marketing Agent - flash sales, coupons, campaign insights (Admin only).
     */
    @PostMapping("/agents/marketing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> marketingAgent(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        return handleAgentRequest(body, authentication.getName(), "marketing",
                marketingAgentService.isEnabled(),
                (adminId, q) -> marketingAgentService.chat(adminId, q));
    }

    /**
     * 🧠 Orchestrator Agent - routes to the most appropriate specialized agent (Admin only).
     */
    @PostMapping("/agents/orchestrator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> orchestratorAgent(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        long start = System.currentTimeMillis();
        try {
            String question = extractQuestion(body);
            if (question == null) {
                return ResponseEntity.ok(errorHandler.handlerException(
                        new IllegalArgumentException("question must not be blank"), start));
            }
            String reply = orchestratorAgentService.orchestrate(authentication.getName(), question);
            return ResponseEntity.ok(successHandler.handlerSuccess(Map.of(
                    "reply", reply != null ? reply : "No response from orchestrator.",
                    "aiEnabled", orchestratorAgentService.isEnabled()), start));
        } catch (Exception e) {
            log.error("Error in orchestrator agent endpoint", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /**
     * Get the status of all AI agents (Admin only).
     */
    @GetMapping("/agents/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApiResponse> getAllAgentsStatus() {
        long start = System.currentTimeMillis();
        Map<String, Object> status = Map.of(
                "support", Map.of("enabled", aiSupportService.isEnabled(), "name", "🔧 Support Agent"),
                "analytics", Map.of("enabled", adminAnalyticsAiService.isEnabled(), "name", "📊 Analytics Agent"),
                "inventory", Map.of("enabled", inventoryAgentService.isEnabled(), "name", "📦 Inventory Agent"),
                "sales", Map.of("enabled", salesAgentService.isEnabled(), "name", "🛒 Sales Agent"),
                "marketing", Map.of("enabled", marketingAgentService.isEnabled(), "name", "🎯 Marketing Agent"),
                "orchestrator", Map.of("enabled", orchestratorAgentService.isEnabled(), "name", "🧠 Orchestrator Agent"));
        return ResponseEntity.ok(successHandler.handlerSuccess(status, start));
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface AgentCaller {
        String call(String adminId, String question);
    }

    private ResponseEntity<BusinessApiResponse> handleAgentRequest(
            Map<String, Object> body,
            String adminId,
            String agentName,
            boolean enabled,
            AgentCaller caller) {
        long start = System.currentTimeMillis();
        try {
            String question = extractQuestion(body);
            if (question == null) {
                return ResponseEntity.ok(errorHandler.handlerException(
                        new IllegalArgumentException("question must not be blank"), start));
            }
            if (!enabled) {
                return ResponseEntity.ok(successHandler.handlerSuccess(Map.of(
                        "reply", String.format("%s AI Agent is not enabled. Configure OPENAI_API_KEY and set app.ai.enabled=true.", agentName),
                        "aiEnabled", false), start));
            }
            String reply = caller.call(adminId, question);
            return ResponseEntity.ok(successHandler.handlerSuccess(Map.of(
                    "reply", reply != null ? reply : "No response from AI agent.",
                    "aiEnabled", true), start));
        } catch (Exception e) {
            log.error("Error in {} agent endpoint", agentName, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    private String extractQuestion(Map<String, Object> body) {
        String question = (String) body.get("question");
        if (question == null) {
            question = (String) body.get("message");
        }
        return (question != null && !question.isBlank()) ? question : null;
    }
}
