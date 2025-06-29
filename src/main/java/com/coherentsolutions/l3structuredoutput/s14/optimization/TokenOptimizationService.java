package com.coherentsolutions.l3structuredoutput.s14.optimization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for optimizing token usage in structured output generation
 * through prompt compression, schema optimization, and intelligent formatting.
 */
@Service
public class TokenOptimizationService {

    private final ObjectMapper objectMapper;
    
    // Common token reduction patterns
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern REDUNDANT_PUNCTUATION = Pattern.compile("([.,;:])\\1+");
    
    // Token estimation constants (rough approximations)
    private static final double AVG_CHARS_PER_TOKEN = 4.0;
    private static final int BASE_SCHEMA_OVERHEAD = 50; // tokens for JSON schema structure

    public TokenOptimizationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * Optimize a prompt for minimal token usage
     */
    public OptimizedPrompt optimizePrompt(String originalPrompt, OptimizationLevel level) {
        String optimized = originalPrompt;
        int originalTokens = estimateTokens(originalPrompt);
        
        switch (level) {
            case MINIMAL:
                optimized = minimalOptimization(originalPrompt);
                break;
            case MODERATE:
                optimized = moderateOptimization(originalPrompt);
                break;
            case AGGRESSIVE:
                optimized = aggressiveOptimization(originalPrompt);
                break;
        }
        
        int optimizedTokens = estimateTokens(optimized);
        
        return new OptimizedPrompt(
                originalPrompt,
                optimized,
                originalTokens,
                optimizedTokens,
                calculateSavingsPercentage(originalTokens, optimizedTokens)
        );
    }

    /**
     * Optimize JSON schema for minimal tokens
     */
    public <T> String optimizeSchema(Class<T> targetClass, SchemaOptimizationOptions options) {
        BeanOutputConverter<T> converter = new BeanOutputConverter<>(targetClass);
        String originalSchema = converter.getFormat();
        
        if (!options.optimizeSchema()) {
            return originalSchema;
        }
        
        String optimized = originalSchema;
        
        if (options.removeDescriptions()) {
            optimized = removeJsonDescriptions(optimized);
        }
        
        if (options.useShortPropertyNames()) {
            optimized = shortenPropertyNames(optimized);
        }
        
        if (options.removeOptionalFields()) {
            optimized = removeOptionalFields(optimized);
        }
        
        return optimized;
    }

    /**
     * Create token-efficient batch prompt
     */
    public String createEfficientBatchPrompt(List<String> items, String instruction) {
        // Use compact format with numbered items
        StringBuilder prompt = new StringBuilder(instruction).append("\n");
        
        for (int i = 0; i < items.size(); i++) {
            prompt.append(i + 1).append(":").append(compressText(items.get(i))).append(";");
        }
        
        return prompt.toString();
    }

    /**
     * Estimate token count for text
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // Simple estimation based on character count and punctuation
        int charCount = text.length();
        int wordCount = text.split("\\s+").length;
        int punctuationCount = text.replaceAll("[^.,;:!?]", "").length();
        
        // Rough estimation formula
        return (int) Math.ceil(charCount / AVG_CHARS_PER_TOKEN) + punctuationCount;
    }

    /**
     * Calculate optimal batch size based on token limits
     */
    public int calculateOptimalBatchSize(
            int maxTokensPerRequest,
            int avgTokensPerItem,
            int responseOverhead) {
        
        int availableTokens = maxTokensPerRequest - responseOverhead - BASE_SCHEMA_OVERHEAD;
        return Math.max(1, availableTokens / avgTokensPerItem);
    }

    /**
     * Compress response format for efficiency
     */
    public <T> CompressedFormat<T> createCompressedFormat(
            Class<T> targetClass,
            Map<String, String> propertyAliases) {
        
        Map<String, String> reverseAliases = propertyAliases.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        
        String compressedSchema = generateCompressedSchema(targetClass, propertyAliases);
        
        return new CompressedFormat<>(
                compressedSchema,
                propertyAliases,
                reverseAliases,
                response -> decompressResponse(response, reverseAliases, targetClass)
        );
    }

    // Optimization implementations

    private String minimalOptimization(String text) {
        // Remove extra whitespace
        return WHITESPACE_PATTERN.matcher(text).replaceAll(" ").trim();
    }

    private String moderateOptimization(String text) {
        String optimized = minimalOptimization(text);
        // Remove redundant punctuation
        optimized = REDUNDANT_PUNCTUATION.matcher(optimized).replaceAll("$1");
        // Remove unnecessary words
        optimized = removeFillerWords(optimized);
        return optimized;
    }

    private String aggressiveOptimization(String text) {
        String optimized = moderateOptimization(text);
        // Abbreviate common phrases
        optimized = abbreviateCommonPhrases(optimized);
        // Use minimal grammar
        optimized = useMinimalGrammar(optimized);
        return optimized;
    }

    private String removeFillerWords(String text) {
        String[] fillers = {"very", "really", "quite", "rather", "somewhat", "fairly"};
        String result = text;
        for (String filler : fillers) {
            result = result.replaceAll("\\b" + filler + "\\b\\s*", "");
        }
        return result;
    }

    private String abbreviateCommonPhrases(String text) {
        Map<String, String> abbreviations = Map.of(
                "for example", "e.g.",
                "that is", "i.e.",
                "and so on", "etc.",
                "versus", "vs.",
                "approximately", "~"
        );
        
        String result = text;
        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String useMinimalGrammar(String text) {
        // Remove articles where not necessary
        return text.replaceAll("\\b(the|a|an)\\b\\s*", "");
    }

    private String removeJsonDescriptions(String schema) {
        // Simple removal of description fields
        return schema.replaceAll("\"description\"\\s*:\\s*\"[^\"]*\",?", "");
    }

    private String shortenPropertyNames(String schema) {
        // This would need more sophisticated JSON parsing for real implementation
        return schema;
    }

    private String removeOptionalFields(String schema) {
        // This would need schema analysis to identify truly optional fields
        return schema;
    }

    private String compressText(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    private double calculateSavingsPercentage(int original, int optimized) {
        if (original == 0) return 0.0;
        return ((double) (original - optimized) / original) * 100;
    }

    private String generateCompressedSchema(Class<?> targetClass, Map<String, String> aliases) {
        // Generate a compressed version of the schema using aliases
        // This is a simplified implementation
        return String.format("Use short keys: %s", aliases);
    }

    private <T> T decompressResponse(String compressedResponse, Map<String, String> reverseAliases, Class<T> targetClass) {
        try {
            // Expand abbreviated keys back to full property names
            String expanded = compressedResponse;
            for (Map.Entry<String, String> entry : reverseAliases.entrySet()) {
                expanded = expanded.replace("\"" + entry.getKey() + "\":", "\"" + entry.getValue() + "\":");
            }
            return objectMapper.readValue(expanded, targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress response", e);
        }
    }

    /**
     * Optimization level enum
     */
    public enum OptimizationLevel {
        MINIMAL,    // Basic whitespace and formatting
        MODERATE,   // Remove filler words and redundancy
        AGGRESSIVE  // Maximum compression with abbreviations
    }

    /**
     * Optimized prompt result
     */
    public record OptimizedPrompt(
            String original,
            String optimized,
            int originalTokens,
            int optimizedTokens,
            double savingsPercentage
    ) {}

    /**
     * Schema optimization options
     */
    public record SchemaOptimizationOptions(
            boolean optimizeSchema,
            boolean removeDescriptions,
            boolean useShortPropertyNames,
            boolean removeOptionalFields
    ) {
        public static SchemaOptimizationOptions defaults() {
            return new SchemaOptimizationOptions(true, true, false, false);
        }
        
        public static SchemaOptimizationOptions aggressive() {
            return new SchemaOptimizationOptions(true, true, true, true);
        }
    }

    /**
     * Compressed format with decompression logic
     */
    public record CompressedFormat<T>(
            String compressedSchema,
            Map<String, String> propertyAliases,
            Map<String, String> reverseAliases,
            ResponseDecompressor<T> decompressor
    ) {}

    /**
     * Response decompressor interface
     */
    @FunctionalInterface
    public interface ResponseDecompressor<T> {
        T decompress(String compressedResponse);
    }
}