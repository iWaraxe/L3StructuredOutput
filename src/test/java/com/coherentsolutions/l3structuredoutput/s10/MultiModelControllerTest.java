package com.coherentsolutions.l3structuredoutput.s10;

import com.coherentsolutions.l3structuredoutput.s10.models.CodeAnalysis;
import com.coherentsolutions.l3structuredoutput.s10.models.UniversalResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MultiModelController.class)
class MultiModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MultiModelService multiModelService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void analyzeCode_ShouldReturnCodeAnalysis() throws Exception {
        // Given
        MultiModelController.CodeAnalysisRequest request = 
            new MultiModelController.CodeAnalysisRequest("def hello(): pass", "python");
        
        CodeAnalysis mockAnalysis = new CodeAnalysis(
                "python",
                CodeAnalysis.ComplexityLevel.LOW,
                "Simple hello function",
                List.of(new CodeAnalysis.Issue(
                        CodeAnalysis.Severity.INFO,
                        "style",
                        1,
                        "Function has no implementation",
                        "Add function body"
                )),
                List.of("Add function documentation"),
                new CodeAnalysis.CodeMetrics(1, 1, 1, 0, 0.0)
        );

        when(multiModelService.analyzeCodeWithOpenAI(anyString(), anyString()))
                .thenReturn(mockAnalysis);

        // When & Then
        mockMvc.perform(post("/api/multi-model/analyze-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("python"))
                .andExpect(jsonPath("$.complexity").value("LOW"))
                .andExpect(jsonPath("$.summary").value("Simple hello function"))
                .andExpect(jsonPath("$.issues[0].severity").value("INFO"))
                .andExpect(jsonPath("$.metrics.linesOfCode").value(1));
    }

    @Test
    void generateWithProvider_ShouldReturnUniversalResponse() throws Exception {
        // Given
        MultiModelController.PromptRequest request = 
            new MultiModelController.PromptRequest("Explain AI");
        
        UniversalResponse mockResponse = new UniversalResponse(
                UUID.randomUUID().toString(),
                "openai",
                "gpt-4",
                LocalDateTime.now(),
                new UniversalResponse.ResponseContent("explanation", "AI is...", "text", 0.95),
                Map.of("version", "v1"),
                new UniversalResponse.UsageInfo(50, 100, 150)
        );

        when(multiModelService.generateStructuredOutput(anyString(), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/multi-model/generate?provider=openai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("openai"))
                .andExpect(jsonPath("$.model").value("gpt-4"))
                .andExpect(jsonPath("$.content.type").value("explanation"))
                .andExpect(jsonPath("$.content.confidence").value(0.95));
    }

    @Test
    void compareProviders_ShouldReturnComparison() throws Exception {
        // Given
        Map<String, Object> mockComparison = Map.of(
                "openai", Map.of("json_mode", true, "structured_output", "excellent"),
                "anthropic", Map.of("json_mode", false, "structured_output", "very_good")
        );

        when(multiModelService.compareProviders(anyString())).thenReturn(mockComparison);

        // When & Then
        mockMvc.perform(get("/api/multi-model/compare-providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openai.json_mode").value(true))
                .andExpect(jsonPath("$.anthropic.structured_output").value("very_good"));
    }

    @Test
    void generateWithAzure_ShouldReturnResponse() throws Exception {
        // Given
        MultiModelController.TaskRequest request = 
            new MultiModelController.TaskRequest("Test prompt", "analysis");
        
        UniversalResponse mockResponse = new UniversalResponse(
                UUID.randomUUID().toString(),
                "azure",
                "gpt-4",
                LocalDateTime.now(),
                new UniversalResponse.ResponseContent("analysis", "Result", "text", 0.95),
                Map.of("region", "eastus"),
                new UniversalResponse.UsageInfo(60, 80, 140)
        );

        when(multiModelService.generateWithAzure(anyString(), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/multi-model/azure/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("azure"))
                .andExpect(jsonPath("$.metadata.region").value("eastus"));
    }

    @Test
    void generateWithAnthropic_ShouldReturnResponse() throws Exception {
        // Given
        MultiModelController.TaskRequest request = 
            new MultiModelController.TaskRequest("Test prompt", "reasoning");
        
        UniversalResponse mockResponse = new UniversalResponse(
                UUID.randomUUID().toString(),
                "anthropic",
                "claude-3-opus",
                LocalDateTime.now(),
                new UniversalResponse.ResponseContent("reasoning", "Complex analysis", "markdown", 0.98),
                Map.of("version", "2024-01"),
                null
        );

        when(multiModelService.generateWithAnthropic(anyString(), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/multi-model/anthropic/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("anthropic"))
                .andExpect(jsonPath("$.model").value("claude-3-opus"))
                .andExpect(jsonPath("$.content.confidence").value(0.98));
    }

    @Test
    void generateWithMistral_ShouldReturnResponse() throws Exception {
        // Given
        MultiModelController.TaskRequest request = 
            new MultiModelController.TaskRequest("Test prompt", "translation");
        
        UniversalResponse mockResponse = new UniversalResponse(
                UUID.randomUUID().toString(),
                "mistral",
                "mistral-large",
                LocalDateTime.now(),
                new UniversalResponse.ResponseContent("translation", "Translated text", "json", 0.92),
                Map.of("endpoint", "eu"),
                new UniversalResponse.UsageInfo(40, 60, 100)
        );

        when(multiModelService.generateWithMistral(anyString(), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/multi-model/mistral/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("mistral"))
                .andExpect(jsonPath("$.content.format").value("json"));
    }

    @Test
    void generateWithOllama_ShouldReturnResponse() throws Exception {
        // Given
        MultiModelController.TaskRequest request = 
            new MultiModelController.TaskRequest("Test prompt", "local-chat");
        
        UniversalResponse mockResponse = new UniversalResponse(
                UUID.randomUUID().toString(),
                "ollama",
                "llama2:13b",
                LocalDateTime.now(),
                new UniversalResponse.ResponseContent("local-chat", "Local response", "plain", 0.85),
                Map.of("local", true, "gpu", "enabled"),
                new UniversalResponse.UsageInfo(30, 40, 70)
        );

        when(multiModelService.generateWithOllama(anyString(), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/multi-model/ollama/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("ollama"))
                .andExpect(jsonPath("$.metadata.local").value(true))
                .andExpect(jsonPath("$.content.confidence").value(0.85));
    }

    @Test
    void getBestPractices_ShouldReturnBestPractices() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/multi-model/best-practices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.universal_models").exists())
                .andExpect(jsonPath("$.provider_abstraction").exists())
                .andExpect(jsonPath("$.performance.cache_schemas").exists())
                .andExpect(jsonPath("$.tips.openai").exists())
                .andExpect(jsonPath("$.tips.anthropic").exists());
    }

    @Test
    void generateWithProvider_WithDefaultProvider_ShouldUseOpenAI() throws Exception {
        // Given
        MultiModelController.PromptRequest request = 
            new MultiModelController.PromptRequest("Default test");
        
        UniversalResponse mockResponse = new UniversalResponse(
                UUID.randomUUID().toString(),
                "openai",
                "gpt-4",
                LocalDateTime.now(),
                new UniversalResponse.ResponseContent("text", "Response", "plain", 0.95),
                Map.of(),
                null
        );

        when(multiModelService.generateStructuredOutput(anyString(), anyString()))
                .thenReturn(mockResponse);

        // When & Then - No provider param, should default to openai
        mockMvc.perform(post("/api/multi-model/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("openai"));
    }
}