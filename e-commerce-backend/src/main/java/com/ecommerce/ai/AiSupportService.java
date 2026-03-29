package com.ecommerce.ai;

import com.ecommerce.ai.tools.BrandTool;
import com.ecommerce.ai.tools.CartTool;
import com.ecommerce.ai.tools.CategoryTool;
import com.ecommerce.ai.tools.CouponTool;
import com.ecommerce.ai.tools.FlashSaleTool;
import com.ecommerce.ai.tools.OrderLookupTool;
import com.ecommerce.ai.tools.ProductSearchTool;
import com.ecommerce.ai.tools.ReviewTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Support Agent Service.
 * <p>
 * Tích hợp LangChain4j + OpenAI để tạo AI agent hỗ trợ khách hàng trong chat.
 * Agent có thể: tìm sản phẩm, tra cứu đơn hàng, xem giỏ hàng, kiểm tra coupon,
 * danh mục sản phẩm, đánh giá sản phẩm, flash sale, thương hiệu.
 * </p>
 */
@Slf4j
@Service
public class AiSupportService {

    @Value("${app.ai.openai.api-key:sk-placeholder}")
    private String openAiApiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.openai.temperature:0.7}")
    private double temperature;

    @Value("${app.ai.openai.max-tokens:512}")
    private int maxTokens;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.memory.max-messages:20}")
    private int memoryMaxMessages;

    @Value("${app.ai.system-prompt:Bạn là trợ lý AI của E-SHOP. Hỗ trợ khách hàng về sản phẩm và đơn hàng.}")
    private String systemPromptText;

    @Autowired
    private ProductSearchTool productSearchTool;

    @Autowired
    private OrderLookupTool orderLookupTool;

    @Autowired
    private CartTool cartTool;

    @Autowired
    private CouponTool couponTool;

    @Autowired
    private CategoryTool categoryTool;

    @Autowired
    private ReviewTool reviewTool;

    @Autowired
    private FlashSaleTool flashSaleTool;

    @Autowired
    private BrandTool brandTool;

    // Per-conversation chat memory (conversationId -> memory)
    private final Map<Long, MessageWindowChatMemory> memoryStore = new ConcurrentHashMap<>();

    private SupportAssistant assistant;

    private static final String PLACEHOLDER_API_KEY = "sk-placeholder";

    /**
     * Keywords that indicate the user wants to speak with a human agent.
     * Used by {@link #isHumanHandoffRequested(String)} for smart handoff detection.
     */
    static final String[] HUMAN_REQUEST_KEYWORDS = {
        "gặp người", "nhân viên", "tư vấn viên", "người thật", "admin",
        "nhân viên hỗ trợ", "hỗ trợ trực tiếp", "liên hệ người", "speak to human",
        "talk to human", "human agent", "real person", "live agent",
        "không muốn nói chuyện với bot", "không phải bot", "cần người hỗ trợ"
    };

    interface SupportAssistant {
        String chat(@MemoryId Long conversationId, @UserMessage String userMessage);
    }

    @PostConstruct
    public void init() {
        if (!aiEnabled) {
            log.info("AI Support Agent is DISABLED. Set app.ai.enabled=true and provide OPENAI_API_KEY to enable.");
            return;
        }

        if (PLACEHOLDER_API_KEY.equals(openAiApiKey) || openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("AI Support Agent: OPENAI_API_KEY not configured. Agent will be disabled.");
            aiEnabled = false;
            return;
        }

        try {
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(model)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .timeout(Duration.ofSeconds(30))
                    .build();

            final String systemPrompt = systemPromptText;
            assistant = AiServices.builder(SupportAssistant.class)
                    .chatLanguageModel(chatModel)
                    .tools(productSearchTool, orderLookupTool, cartTool, couponTool,
                            categoryTool, reviewTool, flashSaleTool, brandTool)
                    .systemMessageProvider(convId -> systemPrompt)
                    .chatMemoryProvider(convId -> memoryStore.computeIfAbsent(
                            (Long) convId,
                            id -> MessageWindowChatMemory.withMaxMessages(memoryMaxMessages)))
                    .build();

            log.info("✅ AI Support Agent initialized successfully with model: {} and {} tools", model, 8);
        } catch (Exception e) {
            log.error("❌ Failed to initialize AI Support Agent: {}", e.getMessage());
            aiEnabled = false;
        }
    }

    /**
     * Process a user message and return the AI response.
     *
     * @param conversationId the conversation context ID (for memory)
     * @param userMessage    the message from the user
     * @return AI response text, or null if AI is disabled
     */
    public String respond(Long conversationId, String userMessage) {
        return respond(conversationId, userMessage, null, null);
    }

    /**
     * Process a user message with injected user context so AI tools can use userId directly.
     *
     * @param conversationId the conversation context ID (for memory)
     * @param userMessage    the message from the user
     * @param userId         the authenticated user's ID (injected as invisible context)
     * @param username       the authenticated user's username
     * @return AI response text, or null if AI is disabled
     */
    public String respond(Long conversationId, String userMessage, Long userId, String username) {
        if (!aiEnabled || assistant == null) {
            return null;
        }
        try {
            // Inject user context as a hidden prefix so AI tools can use it
            String messageWithContext = buildMessageWithContext(userMessage, userId, username);
            log.debug("AI responding in conversation {} for user {}: {}", conversationId, username,
                    userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage);
            String response = assistant.chat(conversationId, messageWithContext);
            log.info("AI responded in conversation {} for user {}", conversationId, username);
            return response;
        } catch (Exception e) {
            log.error("AI error in conversation {}: {}", conversationId, e.getMessage());
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ admin để được hỗ trợ.";
        }
    }

    /**
     * Detect if the user's message is requesting a human agent (smart handoff trigger).
     *
     * @param message the raw user message
     * @return true if human handoff should be triggered
     */
    public boolean isHumanHandoffRequested(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String lower = message.toLowerCase();
        for (String keyword : HUMAN_REQUEST_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Build a message with invisible user context header that the AI can parse.
     * Format: "[UserCtx userId=X username=Y]\n{original message}"
     */
    private String buildMessageWithContext(String userMessage, Long userId, String username) {
        if (userId == null && username == null) {
            return userMessage;
        }
        String ctx = String.format("[UserCtx userId=%s username=%s]",
                userId != null ? userId : "unknown",
                username != null ? username : "unknown");
        return ctx + "\n" + userMessage;
    }

    /**
     * Clear conversation memory when conversation is closed.
     */
    public void clearMemory(Long conversationId) {
        memoryStore.remove(conversationId);
        log.debug("AI memory cleared for conversation {}", conversationId);
    }

    public boolean isEnabled() {
        return aiEnabled && assistant != null;
    }
}
