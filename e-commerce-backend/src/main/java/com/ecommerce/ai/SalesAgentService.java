package com.ecommerce.ai;

import com.ecommerce.ai.tools.SalesAdvisorTool;
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
 * 🛒 Sales Advisor AI Agent.
 * Analyzes sales trends, provides revenue insights, and recommends growth strategies.
 */
@Slf4j
@Service
/**
 * author: LeTuBac
 */
public class SalesAgentService {

    @Value("${app.ai.openai.api-key:sk-placeholder}")
    private String openAiApiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.memory.max-messages:20}")
    private int memoryMaxMessages;

    @Value("${app.ai.sales-system-prompt:You are a Sales Advisor AI agent for E-SHOP. Your role is to analyze sales trends, identify top-performing products, and provide actionable growth recommendations. Highlight key metrics, revenue opportunities, and areas for improvement.}")
    private String systemPrompt;

    @Autowired
    private SalesAdvisorTool salesAdvisorTool;

    private final Map<String, MessageWindowChatMemory> memoryStore = new ConcurrentHashMap<>();
    private SalesAssistant assistant;

    private static final String PLACEHOLDER_API_KEY = "sk-placeholder";

    interface SalesAssistant {
        String chat(@MemoryId String adminId, @UserMessage String question);
    }

    @PostConstruct
    public void init() {
        if (!aiEnabled) {
            log.info("Sales AI Agent is DISABLED. Set app.ai.enabled=true to enable.");
            return;
        }
        if (PLACEHOLDER_API_KEY.equals(openAiApiKey) || openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("Sales AI Agent: OPENAI_API_KEY not configured. Agent will be disabled.");
            aiEnabled = false;
            return;
        }
        try {
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(model)
                    .temperature(0.4)
                    .maxTokens(1024)
                    .timeout(Duration.ofSeconds(60))
                    .build();
            final String prompt = systemPrompt;
            assistant = AiServices.builder(SalesAssistant.class)
                    .chatLanguageModel(chatModel)
                    .tools(salesAdvisorTool)
                    .systemMessageProvider(id -> prompt)
                    .chatMemoryProvider(id -> memoryStore.computeIfAbsent(
                            String.valueOf(id),
                            k -> MessageWindowChatMemory.withMaxMessages(memoryMaxMessages)))
                    .build();
            log.info("✅ Sales AI Agent initialized with model: {}", model);
        } catch (Exception e) {
            log.error("❌ Failed to initialize Sales AI Agent: {}", e.getMessage());
            aiEnabled = false;
        }
    }

    /**
     * Answer a sales-related question.
     *
     * @param adminId  admin session identifier for memory
     * @param question the sales question
     * @return AI response or null if disabled
     */
    public String chat(String adminId, String question) {
        if (!aiEnabled || assistant == null) {
            return null;
        }
        try {
            log.debug("Sales AI responding for admin {}: {}", adminId,
                    question.length() > 80 ? question.substring(0, 80) + "..." : question);
            return assistant.chat(adminId, question);
        } catch (Exception e) {
            log.error("Sales AI Agent error for admin {}: {}", adminId, e.getMessage());
            return "Sorry, the Sales AI encountered an error. Please try again later.";
        }
    }

    public boolean isEnabled() {
        return aiEnabled && assistant != null;
    }
}
