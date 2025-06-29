package com.coherentsolutions.l3structuredoutput.s16.transformation;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Production-ready API response transformation service for converting between different data formats
 */
@Service
public class ApiResponseTransformationService {

    private static final Logger logger = LoggerFactory.getLogger(ApiResponseTransformationService.class);
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, TransformationTemplate> templates = new ConcurrentHashMap<>();
    private final ResponseValidator validator = new ResponseValidator();
    private final PerformanceTracker performanceTracker = new PerformanceTracker();
    
    public ApiResponseTransformationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        initializeTemplates();
    }
    
    /**
     * Transform API response to target format using AI
     */
    public <T> TransformationResult<T> transformResponse(TransformationRequest<T> request) {
        logger.info("Transforming response from {} to {}", 
                request.sourceFormat(), request.targetFormat());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Pre-process source data
            String processedSource = preprocessSource(request.sourceData(), request.sourceFormat());
            
            // Apply transformation
            T transformedData = applyTransformation(processedSource, request);
            
            // Validate result
            ValidationResult validation = validator.validate(transformedData, request.validationRules());
            
            // Post-process if needed
            if (request.enablePostProcessing()) {
                transformedData = postProcessResult(transformedData, request);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            performanceTracker.recordTransformation(request.transformationId(), duration);
            
            return new TransformationResult<>(
                    request.transformationId(),
                    transformedData,
                    validation,
                    duration,
                    TransformationStatus.SUCCESS,
                    null,
                    generateMetadata(request, validation)
            );
            
        } catch (Exception e) {
            logger.error("Transformation failed for {}", request.transformationId(), e);
            long duration = System.currentTimeMillis() - startTime;
            
            return new TransformationResult<>(
                    request.transformationId(),
                    null,
                    new ValidationResult(false, List.of("Transformation failed: " + e.getMessage()), 0.0),
                    duration,
                    TransformationStatus.FAILED,
                    e.getMessage(),
                    Map.of("error", e.getClass().getSimpleName())
            );
        }
    }
    
    /**
     * Transform legacy API response to modern format
     */
    public ModernApiResponse transformLegacyResponse(LegacyApiResponse legacy) {
        BeanOutputConverter<ModernApiResponse> converter = 
                new BeanOutputConverter<>(ModernApiResponse.class);
        
        String prompt = String.format("""
                Transform this legacy API response to modern REST API format:
                
                Legacy Response:
                %s
                
                Convert to modern JSON structure with:
                - Consistent field naming (camelCase)
                - Proper HTTP status codes
                - Standardized error handling
                - RESTful resource representation
                - Pagination metadata where applicable
                """, formatLegacyResponse(legacy));
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Transform between different API versions
     */
    public VersionedApiResponse transformApiVersion(ApiVersionRequest request) {
        BeanOutputConverter<VersionedApiResponse> converter = 
                new BeanOutputConverter<>(VersionedApiResponse.class);
        
        String prompt = String.format("""
                Transform API response from version %s to version %s:
                
                Source Data:
                %s
                
                Version Migration Rules:
                %s
                
                Ensure backward compatibility and proper field mapping.
                """,
                request.sourceVersion(),
                request.targetVersion(),
                request.sourceData(),
                String.join("\n", request.migrationRules())
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Transform GraphQL response to REST format
     */
    public RestApiResponse transformGraphQLToRest(GraphQLResponse graphqlResponse) {
        BeanOutputConverter<RestApiResponse> converter = 
                new BeanOutputConverter<>(RestApiResponse.class);
        
        String prompt = String.format("""
                Convert this GraphQL response to RESTful API format:
                
                GraphQL Response:
                %s
                
                Transform to REST principles:
                - Resource-based URLs
                - Standard HTTP methods
                - Proper status codes
                - Separate endpoints for different resources
                """, formatGraphQLResponse(graphqlResponse));
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Transform REST response to GraphQL format
     */
    public GraphQLResponse transformRestToGraphQL(RestApiResponse restResponse, GraphQLSchema schema) {
        BeanOutputConverter<GraphQLResponse> converter = 
                new BeanOutputConverter<>(GraphQLResponse.class);
        
        String prompt = String.format("""
                Convert this REST API response to GraphQL format:
                
                REST Response:
                %s
                
                GraphQL Schema:
                %s
                
                Map REST fields to GraphQL schema and create proper nested structure.
                """, formatRestResponse(restResponse), schema.definition());
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    /**
     * Batch transformation for multiple responses
     */
    @SuppressWarnings("unchecked")
    public List<TransformationResult<?>> transformBatch(BatchTransformationRequest request) {
        logger.info("Processing batch transformation with {} items", request.items().size());
        
        return request.items().stream()
                .map(item -> (TransformationResult<?>) transformResponse((TransformationRequest<Object>) item))
                .collect(Collectors.toList());
    }
    
    /**
     * Create transformation template
     */
    public void createTransformationTemplate(String templateId, TransformationTemplate template) {
        logger.info("Creating transformation template: {}", templateId);
        templates.put(templateId, template);
    }
    
    /**
     * Apply saved transformation template
     */
    public <T> TransformationResult<T> applyTemplate(String templateId, String sourceData, Class<T> targetClass) {
        TransformationTemplate template = templates.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }
        
        TransformationRequest<T> request = new TransformationRequest<>(
                UUID.randomUUID().toString(),
                sourceData,
                template.sourceFormat(),
                template.targetFormat(),
                targetClass,
                template.transformationRules(),
                template.validationRules(),
                true
        );
        
        return transformResponse(request);
    }
    
    /**
     * Generate OpenAPI specification from API responses
     */
    public OpenApiSpecification generateOpenApiSpec(List<ApiEndpointExample> examples) {
        BeanOutputConverter<OpenApiSpecification> converter = 
                new BeanOutputConverter<>(OpenApiSpecification.class);
        
        String prompt = String.format("""
                Generate OpenAPI 3.0 specification based on these API examples:
                
                %s
                
                Create comprehensive OpenAPI spec with:
                - Proper schemas
                - Path definitions
                - Parameter descriptions
                - Response examples
                - Error responses
                """, formatApiExamples(examples));
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    // Private helper methods
    
    @SuppressWarnings("unchecked")
    private <T> T applyTransformation(String sourceData, TransformationRequest<T> request) {
        // Check if we have a template for this transformation
        String templateKey = request.sourceFormat() + "->" + request.targetFormat();
        TransformationTemplate template = templates.get(templateKey);
        
        if (template != null) {
            return applyTemplateTransformation(sourceData, template, request.targetClass());
        }
        
        // Use AI for dynamic transformation
        BeanOutputConverter<T> converter = new BeanOutputConverter<>(request.targetClass());
        
        String prompt = String.format("""
                Transform this data from %s format to %s format:
                
                Source Data:
                %s
                
                Transformation Rules:
                %s
                
                Ensure data integrity and proper field mapping.
                """,
                request.sourceFormat(),
                request.targetFormat(),
                sourceData,
                String.join("\n", request.transformationRules())
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T applyTemplateTransformation(String sourceData, TransformationTemplate template, Class<T> targetClass) {
        BeanOutputConverter<T> converter = new BeanOutputConverter<>(targetClass);
        
        String prompt = String.format("""
                Apply transformation template: %s
                
                Source Data:
                %s
                
                Template Rules:
                %s
                
                Description: %s
                """,
                template.name(),
                sourceData,
                String.join("\n", template.transformationRules()),
                template.description()
        );
        
        return chatClient.prompt()
                .user(prompt + "\n\n" + converter.getFormat())
                .call()
                .entity(converter);
    }
    
    private String preprocessSource(String sourceData, String sourceFormat) {
        // Apply format-specific preprocessing
        return switch (sourceFormat.toLowerCase()) {
            case "xml" -> preprocessXml(sourceData);
            case "csv" -> preprocessCsv(sourceData);
            case "json" -> preprocessJson(sourceData);
            case "yaml" -> preprocessYaml(sourceData);
            default -> sourceData;
        };
    }
    
    private <T> T postProcessResult(T result, TransformationRequest<T> request) {
        // Apply post-processing rules
        return result; // Simplified for example
    }
    
    private void initializeTemplates() {
        // Initialize common transformation templates
        templates.put("rest->graphql", new TransformationTemplate(
                "REST to GraphQL",
                "Transform REST API responses to GraphQL format",
                "rest",
                "graphql",
                List.of("Map fields to GraphQL schema", "Create nested structure"),
                List.of("Validate GraphQL syntax", "Check schema compliance")
        ));
        
        templates.put("legacy->modern", new TransformationTemplate(
                "Legacy to Modern API",
                "Transform legacy API format to modern REST",
                "legacy",
                "rest",
                List.of("Convert field names to camelCase", "Add proper HTTP status codes"),
                List.of("Validate REST compliance", "Check field completeness")
        ));
    }
    
    private Map<String, Object> generateMetadata(TransformationRequest<?> request, ValidationResult validation) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("transformationId", request.transformationId());
        metadata.put("sourceFormat", request.sourceFormat());
        metadata.put("targetFormat", request.targetFormat());
        metadata.put("validationScore", validation.score());
        metadata.put("timestamp", LocalDateTime.now().toString());
        return metadata;
    }
    
    // Format-specific preprocessing methods
    private String preprocessXml(String xml) { return xml.trim(); }
    private String preprocessCsv(String csv) { return csv.trim(); }
    private String preprocessJson(String json) { 
        try {
            JsonNode node = objectMapper.readTree(json);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return json;
        }
    }
    private String preprocessYaml(String yaml) { return yaml.trim(); }
    
    // Format helper methods
    private String formatLegacyResponse(LegacyApiResponse legacy) {
        return objectMapper.valueToTree(legacy).toString();
    }
    
    private String formatGraphQLResponse(GraphQLResponse response) {
        return objectMapper.valueToTree(response).toString();
    }
    
    private String formatRestResponse(RestApiResponse response) {
        return objectMapper.valueToTree(response).toString();
    }
    
    private String formatApiExamples(List<ApiEndpointExample> examples) {
        return examples.stream()
                .map(example -> String.format("Endpoint: %s %s\nResponse: %s", 
                        example.method(), example.path(), example.responseExample()))
                .collect(Collectors.joining("\n\n"));
    }
    
    // Data models and records
    
    public record TransformationRequest<T>(
            String transformationId,
            String sourceData,
            String sourceFormat,
            String targetFormat,
            Class<T> targetClass,
            List<String> transformationRules,
            List<String> validationRules,
            boolean enablePostProcessing
    ) {}
    
    public record TransformationResult<T>(
            String transformationId,
            T transformedData,
            ValidationResult validation,
            long processingTimeMs,
            TransformationStatus status,
            String errorMessage,
            Map<String, Object> metadata
    ) {}
    
    public record ValidationResult(
            boolean isValid,
            List<String> issues,
            double score
    ) {}
    
    public record TransformationTemplate(
            String name,
            String description,
            String sourceFormat,
            String targetFormat,
            List<String> transformationRules,
            List<String> validationRules
    ) {}
    
    public record BatchTransformationRequest(
            String batchId,
            List<TransformationRequest<?>> items
    ) {}
    
    public record LegacyApiResponse(
            @JsonPropertyDescription("Legacy status code") String status,
            @JsonPropertyDescription("Legacy data format") Map<String, Object> data,
            @JsonPropertyDescription("Legacy error format") String error
    ) {}
    
    public record ModernApiResponse(
            @JsonPropertyDescription("HTTP status code") int statusCode,
            @JsonPropertyDescription("Response data") Map<String, Object> data,
            @JsonPropertyDescription("Error details") ErrorDetails error,
            @JsonPropertyDescription("Response metadata") ResponseMetadata metadata
    ) {}
    
    public record ErrorDetails(
            @JsonPropertyDescription("Error code") String code,
            @JsonPropertyDescription("Error message") String message,
            @JsonPropertyDescription("Error details") Map<String, Object> details
    ) {}
    
    public record ResponseMetadata(
            @JsonPropertyDescription("Response timestamp") String timestamp,
            @JsonPropertyDescription("API version") String version,
            @JsonPropertyDescription("Pagination info") PaginationInfo pagination
    ) {}
    
    public record PaginationInfo(
            @JsonPropertyDescription("Current page") int page,
            @JsonPropertyDescription("Page size") int size,
            @JsonPropertyDescription("Total items") long total,
            @JsonPropertyDescription("Total pages") int totalPages
    ) {}
    
    public record ApiVersionRequest(
            String sourceVersion,
            String targetVersion,
            String sourceData,
            List<String> migrationRules
    ) {}
    
    public record VersionedApiResponse(
            @JsonPropertyDescription("API version") String version,
            @JsonPropertyDescription("Response data") Map<String, Object> data,
            @JsonPropertyDescription("Version metadata") Map<String, String> versionMetadata
    ) {}
    
    public record GraphQLResponse(
            @JsonPropertyDescription("GraphQL data") Map<String, Object> data,
            @JsonPropertyDescription("GraphQL errors") List<Map<String, Object>> errors,
            @JsonPropertyDescription("GraphQL extensions") Map<String, Object> extensions
    ) {}
    
    public record RestApiResponse(
            @JsonPropertyDescription("HTTP status code") int statusCode,
            @JsonPropertyDescription("Response headers") Map<String, String> headers,
            @JsonPropertyDescription("Response body") Map<String, Object> body
    ) {}
    
    public record GraphQLSchema(
            @JsonPropertyDescription("Schema definition") String definition,
            @JsonPropertyDescription("Schema types") List<String> types
    ) {}
    
    public record ApiEndpointExample(
            String method,
            String path,
            Map<String, String> headers,
            String requestExample,
            String responseExample
    ) {}
    
    public record OpenApiSpecification(
            @JsonPropertyDescription("OpenAPI version") String openapi,
            @JsonPropertyDescription("API info") Map<String, Object> info,
            @JsonPropertyDescription("API paths") Map<String, Object> paths,
            @JsonPropertyDescription("Component schemas") Map<String, Object> components
    ) {}
    
    public enum TransformationStatus {
        SUCCESS, FAILED, PARTIAL, PENDING
    }
    
    // Helper classes
    
    private static class ResponseValidator {
        public ValidationResult validate(Object data, List<String> rules) {
            // Implement validation logic
            List<String> issues = new ArrayList<>();
            double score = 1.0;
            
            if (data == null) {
                issues.add("Transformed data is null");
                score = 0.0;
            }
            
            return new ValidationResult(issues.isEmpty(), issues, score);
        }
    }
    
    private static class PerformanceTracker {
        private final Map<String, List<Long>> transformationTimes = new ConcurrentHashMap<>();
        
        public void recordTransformation(String transformationId, long duration) {
            transformationTimes.computeIfAbsent(transformationId, k -> new ArrayList<>()).add(duration);
        }
        
        public double getAverageTime(String transformationId) {
            List<Long> times = transformationTimes.get(transformationId);
            return times == null ? 0.0 : times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
    }
    
    public static class TransformationException extends RuntimeException {
        public TransformationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}