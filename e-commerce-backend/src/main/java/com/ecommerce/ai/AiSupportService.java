package com.ecommerce.ai;

import com.ecommerce.ai.tools.CartTool;
import com.ecommerce.ai.tools.CategoryTool;
import com.ecommerce.ai.tools.CouponTool;
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
 * Agent có thể: tìm sản phẩm, tra cứu đơn hàng, xem giỏ hàng, kiểm tra coupon, danh mục sản phẩm, đánh giá sản phẩm.
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

    // Per-conversation chat memory (conversationId -> memory)
    private final Map<Long, MessageWindowChatMemory> memoryStore = new ConcurrentHashMap<>();

    private SupportAssistant assistant;

    private static final String PLACEHOLDER_API_KEY = "sk-placeholder";
     
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
                    .tools(productSearchTool, orderLookupTool, cartTool, couponTool, categoryTool, reviewTool)
                    .systemMessageProvider(convId -> systemPrompt)
                    .chatMemoryProvider(convId -> memoryStore.computeIfAbsent(
                            (Long) convId,
                            id -> MessageWindowChatMemory.withMaxMessages(memoryMaxMessages)))
                    .build();

            log.info("✅ AI Support Agent initialized successfully with model: {} and {} tools", model, 6);
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
        if (!aiEnabled || assistant == null) {
            return null;
        }
        try {
            log.debug("AI responding in conversation {}: {}", conversationId,
                    userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage);
            String response = assistant.chat(conversationId, userMessage);
            log.info("AI responded in conversation {}", conversationId);
            return response;
        } catch (Exception e) {
            log.error("AI error in conversation {}: {}", conversationId, e.getMessage());
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ admin để được hỗ trợ.";
        }
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
