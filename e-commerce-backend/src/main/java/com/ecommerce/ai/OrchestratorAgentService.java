package com.ecommerce.ai;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 🧠 Orchestrator AI Agent.
 * <p>
 * Routes admin queries to the most appropriate specialized agent:
 * Analytics, Inventory, Sales, or Marketing.
 * Uses keyword-based intent detection with LLM fallback routing.
 * </p>
 */
@Slf4j
@Service
public class OrchestratorAgentService {

    @Value("${app.ai.openai.api-key:sk-placeholder}")
    private String openAiApiKey;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Autowired
    private AdminAnalyticsAiService analyticsAgent;

    @Autowired
    private InventoryAgentService inventoryAgent;

    @Autowired
    private SalesAgentService salesAgent;

    @Autowired
    private MarketingAgentService marketingAgent;

    private OpenAiChatModel classifierModel;

    private static final String PLACEHOLDER_API_KEY = "sk-placeholder";

    private static final String CLASSIFIER_PROMPT =
            "You are an intent classifier for an e-commerce admin system. "
            + "Given a user query, respond with ONLY one of these words: "
            + "ANALYTICS, INVENTORY, SALES, MARKETING. "
            + "Rules: "
            + "- INVENTORY: stock levels, low stock, out of stock, restock, warehouse, tồn kho, hết hàng "
            + "- SALES: revenue, orders, top products, conversion, doanh thu, đơn hàng, bán hàng "
            + "- MARKETING: flash sale, coupon, promotion, campaign, discount, khuyến mãi, voucher "
            + "- ANALYTICS: dashboard, statistics, overview, trends, users, reports, phân tích, thống kê "
            + "When unclear, respond ANALYTICS.";

    @PostConstruct
    public void init() {
        if (!aiEnabled) {
            log.info("Orchestrator AI Agent is DISABLED.");
            return;
        }
        if (PLACEHOLDER_API_KEY.equals(openAiApiKey) || openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("Orchestrator AI Agent: OPENAI_API_KEY not configured. Will use keyword-only routing.");
            aiEnabled = false;
            return;
        }
        try {
            classifierModel = OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(model)
                    .temperature(0.0)
                    .maxTokens(10)
                    .timeout(Duration.ofSeconds(10))
                    .build();
            log.info("✅ Orchestrator AI Agent initialized with model: {}", model);
        } catch (Exception e) {
            log.error("❌ Failed to initialize Orchestrator AI Agent: {}", e.getMessage());
            aiEnabled = false;
        }
    }

    /**
     * Route a query to the most appropriate specialized agent and return its response.
     *
     * @param adminId admin session identifier
     * @param query   the admin's question
     * @return response from the routed agent
     */
    public String orchestrate(String adminId, String query) {
        if (query == null || query.isBlank()) {
            return "Vui lòng nhập câu hỏi của bạn.";
        }

        AgentType agentType = classifyIntent(query);
        log.info("Orchestrator routing query from admin {} to {} agent", adminId, agentType);

        String response = switch (agentType) {
            case INVENTORY -> inventoryAgent.chat(adminId + "_inventory", query);
            case SALES -> salesAgent.chat(adminId + "_sales", query);
            case MARKETING -> marketingAgent.chat(adminId + "_marketing", query);
            default -> analyticsAgent.analyzeData(adminId + "_analytics", query);
        };

        if (response == null) {
            return String.format(
                    "⚠️ AI Agent chưa được kích hoạt. Truy vấn của bạn đã được định tuyến đến **%s Agent**. "
                    + "Vui lòng cấu hình OPENAI_API_KEY và bật app.ai.enabled=true.",
                    agentType.getDisplayName());
        }

        return String.format("[%s]\n\n%s", agentType.getDisplayName(), response);
    }

    /**
     * Classify intent using keyword rules first, then LLM if available.
     */
    private AgentType classifyIntent(String query) {
        // Fast keyword-based classification
        String lower = query.toLowerCase();

        if (containsAny(lower, "tồn kho", "hết hàng", "stock", "inventory", "restock",
                "nhập hàng", "kho", "còn hàng", "low stock")) {
            return AgentType.INVENTORY;
        }
        if (containsAny(lower, "flash sale", "flashsale", "coupon", "voucher", "khuyến mãi",
                "mã giảm giá", "promotion", "campaign", "chiến dịch", "marketing")) {
            return AgentType.MARKETING;
        }
        if (containsAny(lower, "doanh thu", "revenue", "bán hàng", "sales", "đơn hàng",
                "order", "top sản phẩm", "top product", "conversion", "lợi nhuận")) {
            return AgentType.SALES;
        }
        if (containsAny(lower, "phân tích", "analytics", "thống kê", "statistics",
                "dashboard", "báo cáo", "report", "tổng quan", "overview", "xu hướng", "trend")) {
            return AgentType.ANALYTICS;
        }

        // Use LLM classifier if keywords didn't match
        if (classifierModel != null) {
            try {
                var response = classifierModel.generate(
                        SystemMessage.from(CLASSIFIER_PROMPT),
                        UserMessage.from(query));
                String label = response.content().text().trim().toUpperCase();
                return switch (label) {
                    case "INVENTORY" -> AgentType.INVENTORY;
                    case "SALES" -> AgentType.SALES;
                    case "MARKETING" -> AgentType.MARKETING;
                    default -> AgentType.ANALYTICS;
                };
            } catch (Exception e) {
                log.warn("Orchestrator LLM classifier failed, defaulting to ANALYTICS: {}", e.getMessage());
            }
        }

        return AgentType.ANALYTICS;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    public boolean isEnabled() {
        return aiEnabled || (analyticsAgent.isEnabled() || inventoryAgent.isEnabled()
                || salesAgent.isEnabled() || marketingAgent.isEnabled());
    }

    /**
     * Enum representing each specialized agent type.
     */
    public enum AgentType {
        ANALYTICS("📊 Analytics"),
        INVENTORY("📦 Inventory"),
        SALES("🛒 Sales"),
        MARKETING("🎯 Marketing");

        private final String displayName;

        AgentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
