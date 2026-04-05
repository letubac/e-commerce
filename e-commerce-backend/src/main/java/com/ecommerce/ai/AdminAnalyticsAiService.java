package com.ecommerce.ai;

import com.ecommerce.ai.tools.DashboardAnalyticsTool;
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
 * AI Analytics Agent for admin users.
 * Uses LangChain4j + OpenAI to answer analytics and reporting questions.
 */
@Slf4j
@Service
/**
 * author: LeTuBac
 */
public class AdminAnalyticsAiService {

    @Value("${app.ai.openai.api-key:sk-placeholder}")
    private String openAiApiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.openai.temperature:0.3}")
    private double temperature;

    @Value("${app.ai.openai.max-tokens:1024}")
    private int maxTokens;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.memory.max-messages:20}")
    private int memoryMaxMessages;

    @Value("${app.ai.analytics-system-prompt:You are an analytics AI assistant for the E-SHOP admin dashboard. Help admins understand business data, trends, and insights.}")
    private String analyticsSystemPrompt;

    @Autowired
    private DashboardAnalyticsTool dashboardAnalyticsTool;

    private final Map<String, MessageWindowChatMemory> memoryStore = new ConcurrentHashMap<>();

    private AnalyticsAssistant assistant;

    private static final String PLACEHOLDER_API_KEY = "sk-placeholder";

    interface AnalyticsAssistant {
        String analyze(@MemoryId String adminId, @UserMessage String question);
    }

    @PostConstruct
    public void init() {
        if (!aiEnabled) {
            log.info("Admin Analytics AI Agent is DISABLED. Set app.ai.enabled=true to enable.");
            return;
        }

        if (PLACEHOLDER_API_KEY.equals(openAiApiKey) || openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("Admin Analytics AI Agent: OPENAI_API_KEY not configured. Agent will be disabled.");
            aiEnabled = false;
            return;
        }

        try {
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(model)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .build();

            final String systemPrompt = analyticsSystemPrompt;
            assistant = AiServices.builder(AnalyticsAssistant.class)
                    .chatLanguageModel(chatModel)
                    .tools(dashboardAnalyticsTool)
                    .systemMessageProvider(adminId -> systemPrompt)
                    .chatMemoryProvider(adminId -> memoryStore.computeIfAbsent(
                            String.valueOf(adminId),
                            id -> MessageWindowChatMemory.withMaxMessages(memoryMaxMessages)))
                    .build();

            log.info("✅ Admin Analytics AI Agent initialized with model: {}", model);
        } catch (Exception e) {
            log.error("❌ Failed to initialize Admin Analytics AI Agent: {}", e.getMessage());
            aiEnabled = false;
        }
    }

    /**
     * Analyze data based on admin's question.
     *
     * @param adminId  the admin's identifier (for session memory)
     * @param question the analytics question
     * @return AI response
     */
    public String analyzeData(String adminId, String question) {
        if (!aiEnabled || assistant == null) {
            return null;
        }
        try {
            log.debug("Analytics AI responding for admin {}: {}", adminId,
                    question.length() > 80 ? question.substring(0, 80) + "..." : question);
            return assistant.analyze(adminId, question);
        } catch (Exception e) {
            log.error("Analytics AI error for admin {}: {}", adminId, e.getMessage());
            return "Sorry, the analytics AI encountered an error. Please try again later.";
        }
    }

    public boolean isEnabled() {
        return aiEnabled && assistant != null;
    }
}
