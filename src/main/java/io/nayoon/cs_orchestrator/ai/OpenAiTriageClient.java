package io.nayoon.cs_orchestrator.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nayoon.cs_orchestrator.domain.TriageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class OpenAiTriageClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    public OpenAiTriageClient(@Value("${openai.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<TriageResult> triage(String ticketId, String channel, Long customerId, String messageText) {
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "input", new Object[]{
                        Map.of(
                                "role", "system",
                                "content", "You are a triage classifier for customer support. Output MUST match the provided JSON schema."
                        ),
                        Map.of(
                                "role", "user",
                                "content",
                                "Classify this customer message.\n" +
                                        "ticket_id: " + ticketId + "\n" +
                                        "channel: " + channel + "\n" +
                                        "customer_id: " + customerId + "\n" +
                                        "message: " + messageText + "\n"
                        )
                },
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "cs_triage_result",
                                "strict", true,
                                "schema", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "intent", Map.of("type", "string"),
                                                "confidence", Map.of("type", "number", "minimum", 0, "maximum", 1),
                                                "risk_level", Map.of("type", "string", "enum", new String[]{"low","medium","high"}),
                                                "sensitive", Map.of("type", "boolean"),
                                                "emotion", Map.of("type", "string", "enum", new String[]{"neutral","angry","sad","happy"}),
                                                "has_evidence", Map.of("type", "boolean"),
                                                "recommended_action", Map.of("type", "string", "enum", new String[]{"AUTO","DRAFT","HUMAN"}),
                                                "rationale", Map.of("type", "string", "maxLength", 240)
                                        ),
                                        "required", new String[]{
                                                "intent","confidence","risk_level","sensitive","emotion","has_evidence","recommended_action","rationale"
                                        }
                                )
                        )
                ),
                "max_output_tokens", 300
        );

        return webClient.post()
                .uri("/responses")
                .bodyValue(body)
                .retrieve()
                .onStatus(s -> s.isError(), resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(msg -> Mono.error(new IllegalStateException("OpenAI error: " + msg)))
                )
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(12))
                .map(this::extractJsonFromResponsesApi)
                .map(json -> objectMapper.convertValue(json, TriageResult.class));
    }

    private JsonNode extractJsonFromResponsesApi(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode output = root.path("output");
            for (JsonNode item : output) {
                JsonNode contentArr = item.path("content");
                for (JsonNode c : contentArr) {
                    if ("output_text".equals(c.path("type").asText())) {
                        String text = c.path("text").asText();
                        return objectMapper.readTree(text);
                    }
                }
            }
            throw new IllegalStateException("No output_text found in Responses API response");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Responses API response: " + e.getMessage(), e);
        }
    }
}
