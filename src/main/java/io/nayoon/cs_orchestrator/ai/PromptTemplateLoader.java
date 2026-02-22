package io.nayoon.cs_orchestrator.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptTemplateLoader {

    private final String triageSystemPrompt;

    public PromptTemplateLoader() {
        this.triageSystemPrompt = readClasspathText("prompts/triage_system_prompt_v1.txt");
    }

    public String triageSystemPrompt() {
        return triageSystemPrompt;
    }

    private String readClasspathText(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt template: " + path, e);
        }
    }
}