package com.ecommerce.ai;

import com.ecommerce.ai.tools.MarketingTool;
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
 * 🎯 Marketing AI Agent.
 * Manages flash sales, analyzes coupon performance, and suggests marketing campaigns.
 */
@Slf4j
@Service
/**
 * author: LeTuBac
 */
public class MarketingAgentService {

    @Value("${app.ai.openai.api-key:sk-placeholder}")
    private String openAiApiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.memory.max-messages:20}")
    private int memoryMaxMessages;

    @Value("${app.ai.marketing-system-prompt:You are a Marketing AI agent for E-SHOP. Your role is to analyze flash sale performance, evaluate coupon effectiveness, and suggest marketing campaigns and promotions. Help maximize revenue through targeted promotions and customer engagement strategies.}")
    private String systemPrompt;

    @Autowired
    private MarketingTool marketingTool;

    private final Map<String, MessageWindowChatMemory> memoryStore = new ConcurrentHashMap<>();
    private MarketingAssistant assistant;

    private static final String PLACEHOLDER_API_KEY = "sk-placeholder";

    interface MarketingAssistant {
        String chat(@MemoryId String adminId, @UserMessage String question);
    }

    @PostConstruct
    public void init() {
        if (!aiEnabled) {
            log.info("Marketing AI Agent is DISABLED. Set app.ai.enabled=true to enable.");
            return;
        }
        if (PLACEHOLDER_API_KEY.equals(openAiApiKey) || openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("Marketing AI Agent: OPENAI_API_KEY not configured. Agent will be disabled.");
            aiEnabled = false;
            return;
        }
        try {
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(model)
                    .temperature(0.6)
                    .maxTokens(1024)
                    .timeout(Duration.ofSeconds(60))
                    .build();
            final String prompt = systemPrompt;
            assistant = AiServices.builder(MarketingAssistant.class)
                    .chatLanguageModel(chatModel)
                    .tools(marketingTool)
                    .systemMessageProvider(id -> prompt)
                    .chatMemoryProvider(id -> memoryStore.computeIfAbsent(
                            String.valueOf(id),
                            k -> MessageWindowChatMemory.withMaxMessages(memoryMaxMessages)))
                    .build();
            log.info("✅ Marketing AI Agent initialized with model: {}", model);
        } catch (Exception e) {
            log.error("❌ Failed to initialize Marketing AI Agent: {}", e.getMessage());
            aiEnabled = false;
        }
    }

    /**
     * Answer a marketing-related question.
     *
     * @param adminId  admin session identifier for memory
     * @param question the marketing question
     * @return AI response or null if disabled
     */
    public String chat(String adminId, String question) {
        if (!aiEnabled || assistant == null) {
            return null;
        }
        try {
            log.debug("Marketing AI responding for admin {}: {}", adminId,
                    question.length() > 80 ? question.substring(0, 80) + "..." : question);
            return assistant.chat(adminId, question);
        } catch (Exception e) {
            log.error("Marketing AI Agent error for admin {}: {}", adminId, e.getMessage());
            return "Sorry, the Marketing AI encountered an error. Please try again later.";
        }
    }

    public boolean isEnabled() {
        return aiEnabled && assistant != null;
    }
}
