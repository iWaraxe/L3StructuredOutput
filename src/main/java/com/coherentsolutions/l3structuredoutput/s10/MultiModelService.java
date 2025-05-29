package com.coherentsolutions.l3structuredoutput.s10;

import com.coherentsolutions.l3structuredoutput.s10.models.CodeAnalysis;
import com.coherentsolutions.l3structuredoutput.s10.models.UniversalResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service demonstrating multi-model support for structured output.
 * Shows how to use different AI providers with the same structured output approach.
 * 
 * Note: In a real application, you would have actual implementations for each provider.
 * This example focuses on the patterns and approaches for multi-model support.
 */
@Service
public class MultiModelService {

    private final OpenAiChatModel openAiChatModel;
    // In a real app, you would inject these:
    // private final AzureOpenAiChatModel azureOpenAiChatModel;
    // private final AnthropicChatModel anthropicChatModel;
    // private final MistralAiChatModel mistralChatModel;
    // private final OllamaChatModel ollamaChatModel;

    public MultiModelService(@Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    /**
     * Analyzes code using OpenAI with JSON mode for structured output.
     */
    public CodeAnalysis analyzeCodeWithOpenAI(String code, String language) {
        BeanOutputConverter<CodeAnalysis> converter = new BeanOutputConverter<>(CodeAnalysis.class);
        String format = converter.getFormat();

        String promptText = """
            Analyze the following {language} code and provide a detailed analysis.
            
            Code to analyze:
            ```{language}
            {code}
            ```
            
            Provide a comprehensive analysis including:
            - Language identification
            - Complexity assessment
            - Summary of functionality
            - Identified issues with severity levels
            - Improvement suggestions
            - Code metrics
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", code);
        parameters.put("language", language);
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);
        
        // Configure OpenAI for JSON output
        String jsonSchema = converter.getJsonSchema();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_OBJECT, jsonSchema))
                .temperature(0.3)
                .build();
        
        Prompt prompt = new Prompt(renderedPrompt, options);
        ChatResponse response = openAiChatModel.call(prompt);
        
        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }

    /**
     * Simulates using Azure OpenAI for structured output.
     * In a real implementation, you would use AzureOpenAiChatModel.
     */
    public UniversalResponse generateWithAzure(String prompt, String task) {
        // Simulated Azure OpenAI response
        return createUniversalResponse(
                "azure",
                "gpt-4",
                new UniversalResponse.ResponseContent(
                        task,
                        "Azure OpenAI response for: " + task,
                        "text",
                        0.95
                ),
                Map.of("region", "eastus", "deployment", "production")
        );
    }

    /**
     * Simulates using Anthropic Claude for structured output.
     * Anthropic has excellent support for structured output through their API.
     */
    public UniversalResponse generateWithAnthropic(String prompt, String task) {
        // Simulated Anthropic response
        return createUniversalResponse(
                "anthropic",
                "claude-3-opus",
                new UniversalResponse.ResponseContent(
                        task,
                        "Anthropic Claude response with structured output",
                        "markdown",
                        0.98
                ),
                Map.of("version", "2024-01", "capability", "advanced")
        );
    }

    /**
     * Simulates using Mistral AI for structured output.
     * Mistral supports JSON mode similar to OpenAI.
     */
    public UniversalResponse generateWithMistral(String prompt, String task) {
        // Simulated Mistral response
        return createUniversalResponse(
                "mistral",
                "mistral-large",
                new UniversalResponse.ResponseContent(
                        task,
                        "Mistral AI structured response",
                        "json",
                        0.92
                ),
                Map.of("endpoint", "eu", "mode", "json")
        );
    }

    /**
     * Simulates using Ollama (local models) for structured output.
     * Ollama can run various open-source models locally.
     */
    public UniversalResponse generateWithOllama(String prompt, String task) {
        // Simulated Ollama response
        return createUniversalResponse(
                "ollama",
                "llama2:13b",
                new UniversalResponse.ResponseContent(
                        task,
                        "Ollama local model response",
                        "plain",
                        0.85
                ),
                Map.of("local", true, "gpu", "enabled")
        );
    }

    /**
     * Demonstrates provider-agnostic structured output generation.
     * This method can switch between providers based on availability or requirements.
     */
    public UniversalResponse generateStructuredOutput(String prompt, String provider) {
        return switch (provider.toLowerCase()) {
            case "openai" -> generateWithOpenAI(prompt);
            case "azure" -> generateWithAzure(prompt, "general");
            case "anthropic" -> generateWithAnthropic(prompt, "general");
            case "mistral" -> generateWithMistral(prompt, "general");
            case "ollama" -> generateWithOllama(prompt, "general");
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    /**
     * Uses OpenAI directly for general structured output.
     */
    private UniversalResponse generateWithOpenAI(String prompt) {
        BeanOutputConverter<UniversalResponse> converter = new BeanOutputConverter<>(UniversalResponse.class);
        
        // For demonstration, create a structured response
        ChatResponse response = openAiChatModel.call(new Prompt(prompt));
        String content = response.getResult().getOutput().getText();
        
        return createUniversalResponse(
                "openai",
                "gpt-4",
                new UniversalResponse.ResponseContent(
                        "text",
                        content,
                        "plain",
                        0.95
                ),
                Map.of("api_version", "v1", "mode", "standard")
        );
    }

    /**
     * Helper method to create UniversalResponse objects.
     */
    private UniversalResponse createUniversalResponse(
            String provider,
            String model,
            UniversalResponse.ResponseContent content,
            Map<String, Object> metadata) {
        
        return new UniversalResponse(
                UUID.randomUUID().toString(),
                provider,
                model,
                LocalDateTime.now(),
                content,
                metadata,
                new UniversalResponse.UsageInfo(100, 50, 150) // Simulated usage
        );
    }

    /**
     * Demonstrates how to handle provider-specific features while maintaining
     * a common structured output format.
     */
    public Map<String, Object> compareProviders(String prompt) {
        Map<String, Object> comparison = new HashMap<>();
        
        // Features supported by each provider
        comparison.put("openai", Map.of(
                "json_mode", true,
                "json_schema", true,
                "function_calling", true,
                "structured_output", "excellent"
        ));
        
        comparison.put("azure", Map.of(
                "json_mode", true,
                "json_schema", true,
                "function_calling", true,
                "structured_output", "excellent",
                "deployment_options", "flexible"
        ));
        
        comparison.put("anthropic", Map.of(
                "json_mode", false,
                "xml_support", true,
                "structured_prompting", true,
                "structured_output", "very_good"
        ));
        
        comparison.put("mistral", Map.of(
                "json_mode", true,
                "function_calling", true,
                "structured_output", "good"
        ));
        
        comparison.put("ollama", Map.of(
                "json_mode", "model_dependent",
                "local_deployment", true,
                "structured_output", "varies",
                "cost", "free"
        ));
        
        return comparison;
    }
}