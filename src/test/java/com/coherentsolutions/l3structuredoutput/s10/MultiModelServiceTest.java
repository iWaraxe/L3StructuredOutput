package com.coherentsolutions.l3structuredoutput.s10;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import com.coherentsolutions.l3structuredoutput.s10.models.CodeAnalysis;
import com.coherentsolutions.l3structuredoutput.s10.models.UniversalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiModelServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel openAiChatModel;

    private MultiModelService multiModelService;

    @BeforeEach
    void setUp() {
        multiModelService = new MultiModelService(openAiChatModel);
    }

    @Test
    void analyzeCodeWithOpenAI_ShouldReturnCodeAnalysis() {
        // Given
        String code = """
                def factorial(n):
                    if n <= 1:
                        return 1
                    return n * factorial(n - 1)
                """;
        String language = "python";

        String mockResponse = """
                {
                    "language": "python",
                    "complexity": "LOW",
                    "summary": "Recursive factorial function",
                    "issues": [
                        {
                            "severity": "WARNING",
                            "type": "performance",
                            "line": 4,
                            "description": "Recursive implementation may cause stack overflow for large inputs",
                            "suggestion": "Consider using iterative approach for better performance"
                        }
                    ],
                    "suggestions": [
                        "Add input validation for negative numbers",
                        "Consider memoization for optimization"
                    ],
                    "metrics": {
                        "linesOfCode": 4,
                        "cyclomaticComplexity": 2,
                        "functions": 1,
                        "classes": 0,
                        "testCoverage": 0.0
                    }
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CodeAnalysis analysis = multiModelService.analyzeCodeWithOpenAI(code, language);

        // Then
        assertNotNull(analysis);
        assertEquals("python", analysis.language());
        assertEquals(CodeAnalysis.ComplexityLevel.LOW, analysis.complexity());
        assertEquals("Recursive factorial function", analysis.summary());
        assertEquals(1, analysis.issues().size());
        assertEquals(CodeAnalysis.Severity.WARNING, analysis.issues().get(0).severity());
        assertEquals(2, analysis.suggestions().size());
        assertNotNull(analysis.metrics());
        assertEquals(4, analysis.metrics().linesOfCode());
    }

    @Test
    void generateWithAzure_ShouldReturnUniversalResponse() {
        // When
        UniversalResponse response = multiModelService.generateWithAzure("Test prompt", "code-review");

        // Then
        assertNotNull(response);
        assertEquals("azure", response.provider());
        assertEquals("gpt-4", response.model());
        assertNotNull(response.requestId());
        assertNotNull(response.timestamp());
        assertEquals("code-review", response.content().type());
        assertEquals("text", response.content().format());
        assertEquals(0.95, response.content().confidence());
        assertTrue(response.metadata().containsKey("region"));
        assertEquals("eastus", response.metadata().get("region"));
    }

    @Test
    void generateWithAnthropic_ShouldReturnUniversalResponse() {
        // When
        UniversalResponse response = multiModelService.generateWithAnthropic("Test prompt", "analysis");

        // Then
        assertNotNull(response);
        assertEquals("anthropic", response.provider());
        assertEquals("claude-3-opus", response.model());
        assertEquals("analysis", response.content().type());
        assertEquals("markdown", response.content().format());
        assertEquals(0.98, response.content().confidence());
        assertEquals("advanced", response.metadata().get("capability"));
    }

    @Test
    void generateWithMistral_ShouldReturnUniversalResponse() {
        // When
        UniversalResponse response = multiModelService.generateWithMistral("Test prompt", "summary");

        // Then
        assertNotNull(response);
        assertEquals("mistral", response.provider());
        assertEquals("mistral-large", response.model());
        assertEquals("summary", response.content().type());
        assertEquals("json", response.content().format());
        assertEquals(0.92, response.content().confidence());
        assertEquals("eu", response.metadata().get("endpoint"));
    }

    @Test
    void generateWithOllama_ShouldReturnUniversalResponse() {
        // When
        UniversalResponse response = multiModelService.generateWithOllama("Test prompt", "chat");

        // Then
        assertNotNull(response);
        assertEquals("ollama", response.provider());
        assertEquals("llama2:13b", response.model());
        assertEquals("chat", response.content().type());
        assertEquals("plain", response.content().format());
        assertEquals(0.85, response.content().confidence());
        assertEquals(true, response.metadata().get("local"));
    }

    @Test
    void generateStructuredOutput_WithValidProvider_ShouldReturnResponse() {
        // Given
        String mockContent = "Generated content";
        ChatResponse chatResponse = createMockResponse(mockContent);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        UniversalResponse response = multiModelService.generateStructuredOutput("Test prompt", "openai");

        // Then
        assertNotNull(response);
        assertEquals("openai", response.provider());
        assertEquals("gpt-4", response.model());
        assertEquals("Generated content", response.content().data());
    }

    @Test
    void generateStructuredOutput_WithInvalidProvider_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> multiModelService.generateStructuredOutput("Test prompt", "invalid-provider"));
    }

    @Test
    void compareProviders_ShouldReturnProviderComparison() {
        // When
        Map<String, Object> comparison = multiModelService.compareProviders("test");

        // Then
        assertNotNull(comparison);
        assertTrue(comparison.containsKey("openai"));
        assertTrue(comparison.containsKey("azure"));
        assertTrue(comparison.containsKey("anthropic"));
        assertTrue(comparison.containsKey("mistral"));
        assertTrue(comparison.containsKey("ollama"));

        // Verify OpenAI features
        @SuppressWarnings("unchecked")
        Map<String, Object> openaiFeatures = (Map<String, Object>) comparison.get("openai");
        assertEquals(true, openaiFeatures.get("json_mode"));
        assertEquals(true, openaiFeatures.get("json_schema"));
        assertEquals("excellent", openaiFeatures.get("structured_output"));

        // Verify Ollama features
        @SuppressWarnings("unchecked")
        Map<String, Object> ollamaFeatures = (Map<String, Object>) comparison.get("ollama");
        assertEquals("free", ollamaFeatures.get("cost"));
        assertEquals(true, ollamaFeatures.get("local_deployment"));
    }

    @Test
    void analyzeCodeWithOpenAI_WithComplexCode_ShouldHandleMultipleIssues() {
        // Given
        String complexCode = """
                class Calculator:
                    def divide(self, a, b):
                        return a / b
                    
                    def complex_calc(self, x, y, z):
                        if x > 0:
                            if y > 0:
                                if z > 0:
                                    return x * y * z
                        return 0
                """;

        String mockResponse = """
                {
                    "language": "python",
                    "complexity": "HIGH",
                    "summary": "Calculator class with division and complex calculation methods",
                    "issues": [
                        {
                            "severity": "ERROR",
                            "type": "bug",
                            "line": 3,
                            "description": "Division by zero not handled",
                            "suggestion": "Add check for b != 0"
                        },
                        {
                            "severity": "WARNING",
                            "type": "style",
                            "line": 5,
                            "description": "Deeply nested if statements",
                            "suggestion": "Refactor to reduce nesting"
                        }
                    ],
                    "suggestions": [
                        "Add error handling for division by zero",
                        "Refactor nested conditions",
                        "Add type hints"
                    ],
                    "metrics": {
                        "linesOfCode": 10,
                        "cyclomaticComplexity": 4,
                        "functions": 2,
                        "classes": 1,
                        "testCoverage": 0.0
                    }
                }
                """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(openAiChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        CodeAnalysis analysis = multiModelService.analyzeCodeWithOpenAI(complexCode, "python");

        // Then
        assertEquals(CodeAnalysis.ComplexityLevel.HIGH, analysis.complexity());
        assertEquals(2, analysis.issues().size());
        assertEquals(CodeAnalysis.Severity.ERROR, analysis.issues().get(0).severity());
        assertEquals(3, analysis.suggestions().size());
        assertEquals(4, analysis.metrics().cyclomaticComplexity());
    }
}