package com.westlakers.leap_bff.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiAssistantService {
    private static final Map<String, String> TOPIC_CONTENT = Map.of(
        "market-order", "A market order is an instruction to buy or sell an asset immediately at the best available price. The final price can change because markets move and available liquidity varies.",
        "limit-order", "A limit order is an instruction to buy or sell only at a specified price or a more favorable one. It may not execute if the market never reaches that price.",
        "stop-order", "A stop order becomes a market order when a specified stop price is reached. Execution price is not guaranteed and can differ from the stop price in a fast-moving market.",
        "bid-ask", "The bid is the highest price a buyer currently offers, while the ask is the lowest price a seller currently requests. The difference is called the bid-ask spread.",
        "market-hours", "Regular U.S. stock market hours are generally 9:30 a.m. to 4:00 p.m. Eastern Time on business days. Some venues also offer pre-market and after-hours trading with different risks.",
        "diversification", "Diversification means spreading exposure across different investments or categories rather than relying on one investment. It can reduce concentration risk but cannot eliminate investment losses."
    );

    private static final Set<String> UNSAFE_TERMS = Set.of(
        "buy", "sell", "recommend", "recommendation", "should i", "what should i", "price target",
        "target price", "entry price", "exit price", "my portfolio", "my holdings", "my position",
        "ticker", "stock pick", "best stock", "what stock", "invest in"
    );

        private static final Set<String> UNSAFE_OUTPUT_TERMS = Set.of(
            "you should buy", "you should sell", "i recommend", "my recommendation",
            "price target is", "target price is", "buy this stock", "sell this stock"
        );

    private static final Pattern JSON_OBJECT = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    private static final String REFUSAL = "I can explain general trading concepts, but I cannot recommend stocks, trades, price targets, or actions for your portfolio.";

    private static final String ANSWER_SYSTEM_PROMPT = "You are a concise educational trading assistant. "
            + "Answer only general educational questions about the supplied trading concept. "
            + "Never recommend a stock, trade, portfolio action, price target, entry, exit, or timing. "
            + "If the question asks for personalized or actionable investment advice, refuse briefly. "
            + "Mention that this is educational information, not financial advice, when appropriate.";

    private static final String CLASSIFIER_SYSTEM_PROMPT = "Classify the user's general trading education question into exactly one allowed topic ID. "
            + "Return JSON only in the form {\"topic\":\"topic-id\"}. "
            + "Allowed topic IDs: market-order, limit-order, stop-order, bid-ask, market-hours, diversification. "
            + "Return {\"topic\":\"unsupported\"} for investment recommendations, portfolio advice, stock picks, price predictions, or unrelated questions.";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public AiAssistantService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${ai.openai.api-key:}") String apiKey,
            @Value("${ai.openai.base-url:https://generativelanguage.googleapis.com/v1beta/openai}") String baseUrl,
            @Value("${ai.openai.model:gemini-2.5-flash}") String model) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String answer(String userQuestion, String requestedTopic) {
        if (apiKey.isBlank()) {
            throw new ApiException(ErrorCode.AI_ASSISTANT_UNAVAILABLE, "AI assistant is not configured");
        }

        String question = userQuestion == null ? "" : userQuestion.trim();
        if (question.isBlank() && (requestedTopic == null || requestedTopic.isBlank())) {
            throw new ApiException(ErrorCode.MISSING_REQUIRED_FIELD, "A question is required");
        }
        if (containsUnsafeIntent(question)) {
            return REFUSAL;
        }

        String topic = question.isBlank() && requestedTopic != null && !requestedTopic.isBlank()
            ? requestedTopic
            : classify(question);
        String sourceContent = TOPIC_CONTENT.get(topic);
        if (sourceContent == null) {
            return REFUSAL;
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("temperature", 0.2);
        ArrayNode messages = payload.putArray("messages");
        addMessage(messages, "system", ANSWER_SYSTEM_PROMPT);
        addMessage(messages, "user", "Explain this approved reference content in plain language without adding advice:\n" + sourceContent);

        String answer = callModel(payload);
        return containsUnsafeOutput(answer) ? REFUSAL : answer;
    }

    private String classify(String question) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("temperature", 0);
        ArrayNode messages = payload.putArray("messages");
        addMessage(messages, "system", CLASSIFIER_SYSTEM_PROMPT);
        addMessage(messages, "user", question);
        JsonNode response = callModelResponse(payload);
        String content = response.at("/choices/0/message/content").asText("");
        try {
            JsonNode json = objectMapper.readTree(extractJsonObject(content));
            String topic = json.path("topic").asText("unsupported");
            return TOPIC_CONTENT.containsKey(topic) ? topic : "unsupported";
        } catch (Exception exception) {
            return "unsupported";
        }
    }

    private String callModel(ObjectNode payload) {
        JsonNode response = callModelResponse(payload);
        String answer = response.at("/choices/0/message/content").asText(null);
        if (answer == null || answer.isBlank()) {
            throw new ApiException(ErrorCode.AI_ASSISTANT_UNAVAILABLE, "AI assistant returned no answer");
        }
        return answer.trim();
    }

    private JsonNode callModelResponse(ObjectNode payload) {
        try {
            // Serialize with our Jackson 2 ObjectMapper; RestClient's converters in Boot 4 use Jackson 3
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(payload))
                    .retrieve()
                    .body(String.class);
            if (responseBody == null || responseBody.isBlank()) {
                throw new ApiException(ErrorCode.AI_ASSISTANT_UNAVAILABLE, "AI assistant returned no response");
            }
            return objectMapper.readTree(responseBody);
        } catch (ApiException exception) {
            throw exception;
        } catch (JsonProcessingException | RuntimeException exception) {
            log.error("AI model call failed", exception);
            throw new ApiException(ErrorCode.AI_ASSISTANT_UNAVAILABLE, "AI assistant is temporarily unavailable");
        }
    }

    private boolean containsUnsafeIntent(String text) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return UNSAFE_TERMS.stream().anyMatch(normalized::contains);
    }

    private boolean containsUnsafeOutput(String text) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT);
        return UNSAFE_OUTPUT_TERMS.stream().anyMatch(normalized::contains);
    }

    private String extractJsonObject(String content) {
        var matcher = JSON_OBJECT.matcher(content == null ? "" : content);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Classifier did not return JSON");
        }
        return matcher.group();
    }

    private void addMessage(ArrayNode messages, String role, String content) {
        ObjectNode message = messages.addObject();
        message.put("role", role);
        message.put("content", content);
    }
}