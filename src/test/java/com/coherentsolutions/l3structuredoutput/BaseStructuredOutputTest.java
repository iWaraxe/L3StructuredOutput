package com.coherentsolutions.l3structuredoutput;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

/**
 * Base test class providing common utilities for structured output tests.
 */
public abstract class BaseStructuredOutputTest {

    /**
     * Creates a mock ChatResponse with the given text content.
     */
    protected ChatResponse createMockResponse(String content) {
        AssistantMessage message = new AssistantMessage(content);
        Generation generation = new Generation(message);
        return new ChatResponse(List.of(generation), ChatResponseMetadata.builder().build());
    }

    /**
     * Creates a sample JSON response for testing.
     */
    protected String createJsonResponse(String json) {
        return json.replaceAll("\\s+", " ").trim();
    }

    /**
     * Verifies that a prompt contains expected content.
     */
    protected boolean promptContains(Prompt prompt, String... expectedContent) {
        String promptText = prompt.getInstructions().get(0).getText();
        for (String content : expectedContent) {
            if (!promptText.contains(content)) {
                return false;
            }
        }
        return true;
    }
}