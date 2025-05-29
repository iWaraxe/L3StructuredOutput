package com.coherentsolutions.l3structuredoutput.s11;

import com.coherentsolutions.l3structuredoutput.s11.models.OrderRequest;
import com.coherentsolutions.l3structuredoutput.s11.models.ValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service demonstrating validation and error recovery strategies
 * for AI-generated structured output.
 */
@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    
    private final OpenAiChatModel chatModel;
    private final Validator validator;
    private final ObjectMapper objectMapper;

    public ValidationService(OpenAiChatModel chatModel, Validator validator) {
        this.chatModel = chatModel;
        this.validator = validator;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Generates an order request with AI and validates it,
     * applying recovery strategies if needed.
     */
    public ValidationResult generateAndValidateOrder(String orderDescription) {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        List<ValidationResult.ValidationWarning> warnings = new ArrayList<>();
        List<ValidationResult.RecoveryAttempt> recoveryAttempts = new ArrayList<>();
        
        try {
            // Step 1: Generate initial order with AI
            String initialResponse = generateOrderWithAI(orderDescription);
            
            // Step 2: Attempt to parse the response
            OrderRequest order = parseOrderWithRecovery(initialResponse, recoveryAttempts);
            
            if (order == null) {
                return new ValidationResult(
                        false,
                        LocalDateTime.now(),
                        List.of(new ValidationResult.ValidationError(
                                "parsing",
                                "Failed to parse AI response",
                                initialResponse,
                                "valid JSON"
                        )),
                        warnings,
                        recoveryAttempts,
                        null,
                        Map.of("stage", "parsing_failed")
                );
            }
            
            // Step 3: Validate the order
            Set<ConstraintViolation<OrderRequest>> violations = validator.validate(order);
            
            if (!violations.isEmpty()) {
                // Convert violations to errors
                for (ConstraintViolation<OrderRequest> violation : violations) {
                    errors.add(new ValidationResult.ValidationError(
                            violation.getPropertyPath().toString(),
                            violation.getMessage(),
                            violation.getInvalidValue(),
                            violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()
                    ));
                }
                
                // Step 4: Attempt recovery
                order = attemptRecovery(order, violations, recoveryAttempts);
                
                // Re-validate after recovery
                violations = validator.validate(order);
                if (violations.isEmpty()) {
                    logger.info("Recovery successful - all validation errors resolved");
                }
            }
            
            // Step 5: Add warnings for potential issues
            addWarnings(order, warnings);
            
            return new ValidationResult(
                    violations.isEmpty(),
                    LocalDateTime.now(),
                    errors,
                    warnings,
                    recoveryAttempts,
                    order,
                    Map.of(
                            "originalDescription", orderDescription,
                            "recoveryAttempted", !recoveryAttempts.isEmpty()
                    )
            );
            
        } catch (Exception e) {
            logger.error("Unexpected error during validation", e);
            return new ValidationResult(
                    false,
                    LocalDateTime.now(),
                    List.of(new ValidationResult.ValidationError(
                            "system",
                            "Unexpected error: " + e.getMessage(),
                            null,
                            "system_stable"
                    )),
                    warnings,
                    recoveryAttempts,
                    null,
                    Map.of("exception", e.getClass().getSimpleName())
            );
        }
    }

    /**
     * Generates an order using AI.
     */
    private String generateOrderWithAI(String description) {
        BeanOutputConverter<OrderRequest> converter = new BeanOutputConverter<>(OrderRequest.class);
        String format = converter.getFormat();

        String promptText = """
            Generate a complete order based on this description:
            {description}
            
            Requirements:
            - Order ID must follow pattern: ORD-XXXXXX (6 digits)
            - Include valid email address
            - Order date should be today or in the past
            - Include at least one item with valid details
            - Calculate total amount correctly
            - Provide complete shipping address with valid US format
            - Select appropriate payment method
            
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("description", description);
        parameters.put("format", format);

        String renderedPrompt = template.render(parameters);
        Prompt prompt = new Prompt(renderedPrompt);
        
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    /**
     * Attempts to parse the order with recovery strategies.
     */
    private OrderRequest parseOrderWithRecovery(String response, List<ValidationResult.RecoveryAttempt> attempts) {
        BeanOutputConverter<OrderRequest> converter = new BeanOutputConverter<>(OrderRequest.class);
        
        try {
            // First attempt: direct parsing
            return converter.convert(response);
        } catch (Exception e) {
            logger.warn("Initial parsing failed: {}", e.getMessage());
            
            // Recovery attempt 1: Extract JSON from text
            String extractedJson = extractJsonFromText(response);
            if (extractedJson != null && !extractedJson.equals(response)) {
                attempts.add(new ValidationResult.RecoveryAttempt(
                        "extract_json",
                        false,
                        "Extracted JSON from surrounding text",
                        extractedJson
                ));
                
                try {
                    OrderRequest order = converter.convert(extractedJson);
                    attempts.set(attempts.size() - 1, new ValidationResult.RecoveryAttempt(
                            "extract_json",
                            true,
                            "Successfully extracted and parsed JSON",
                            order
                    ));
                    return order;
                } catch (Exception e2) {
                    logger.warn("Parsing extracted JSON failed: {}", e2.getMessage());
                }
            }
            
            // Recovery attempt 2: Fix common JSON issues
            String fixedJson = fixCommonJsonIssues(response);
            if (!fixedJson.equals(response)) {
                attempts.add(new ValidationResult.RecoveryAttempt(
                        "fix_json",
                        false,
                        "Applied common JSON fixes",
                        fixedJson
                ));
                
                try {
                    OrderRequest order = converter.convert(fixedJson);
                    attempts.set(attempts.size() - 1, new ValidationResult.RecoveryAttempt(
                            "fix_json",
                            true,
                            "Successfully fixed and parsed JSON",
                            order
                    ));
                    return order;
                } catch (Exception e3) {
                    logger.warn("Parsing fixed JSON failed: {}", e3.getMessage());
                }
            }
        }
        
        return null;
    }

    /**
     * Attempts to recover from validation errors.
     */
    private OrderRequest attemptRecovery(OrderRequest order, Set<ConstraintViolation<OrderRequest>> violations, 
                                        List<ValidationResult.RecoveryAttempt> attempts) {
        
        for (ConstraintViolation<OrderRequest> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            Object invalidValue = violation.getInvalidValue();
            
            // Recovery strategies based on the type of violation
            if (propertyPath.equals("orderId") && invalidValue != null) {
                String fixedOrderId = fixOrderId(invalidValue.toString());
                if (!fixedOrderId.equals(invalidValue)) {
                    attempts.add(new ValidationResult.RecoveryAttempt(
                            "fix_order_id",
                            true,
                            "Fixed order ID format",
                            fixedOrderId
                    ));
                    order = updateOrderField(order, "orderId", fixedOrderId);
                }
            } else if (propertyPath.equals("orderDate") && invalidValue instanceof LocalDate) {
                LocalDate date = (LocalDate) invalidValue;
                if (date.isAfter(LocalDate.now())) {
                    LocalDate fixedDate = LocalDate.now();
                    attempts.add(new ValidationResult.RecoveryAttempt(
                            "fix_future_date",
                            true,
                            "Changed future date to today",
                            fixedDate
                    ));
                    order = updateOrderField(order, "orderDate", fixedDate);
                }
            } else if (propertyPath.contains("email") && invalidValue != null) {
                String fixedEmail = fixEmail(invalidValue.toString());
                if (!fixedEmail.equals(invalidValue)) {
                    attempts.add(new ValidationResult.RecoveryAttempt(
                            "fix_email",
                            true,
                            "Fixed email format",
                            fixedEmail
                    ));
                    order = updateOrderField(order, "customerEmail", fixedEmail);
                }
            }
        }
        
        return order;
    }

    /**
     * Adds warnings for potential issues that aren't validation errors.
     */
    private void addWarnings(OrderRequest order, List<ValidationResult.ValidationWarning> warnings) {
        // Check for unusually high order amount
        if (order.totalAmount() > 10000) {
            warnings.add(new ValidationResult.ValidationWarning(
                    "totalAmount",
                    "Order amount is unusually high",
                    "Consider verifying the amount with the customer"
            ));
        }
        
        // Check for large quantity orders
        for (OrderRequest.OrderItem item : order.items()) {
            if (item.quantity() > 100) {
                warnings.add(new ValidationResult.ValidationWarning(
                        "items.quantity",
                        "Large quantity ordered for " + item.productName(),
                        "Verify stock availability"
                ));
            }
        }
        
        // Check for cryptocurrency payment
        if (order.paymentMethod() == OrderRequest.PaymentMethod.CRYPTOCURRENCY) {
            warnings.add(new ValidationResult.ValidationWarning(
                    "paymentMethod",
                    "Cryptocurrency payment selected",
                    "Ensure compliance with regulations"
            ));
        }
    }

    /**
     * Extracts JSON from surrounding text.
     */
    private String extractJsonFromText(String text) {
        // Look for JSON object pattern
        Pattern pattern = Pattern.compile("\\{[^{}]*\\{[^{}]*\\}[^{}]*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        // Try simpler pattern
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return null;
    }

    /**
     * Fixes common JSON issues.
     */
    private String fixCommonJsonIssues(String json) {
        String fixed = json;
        
        // Fix single quotes to double quotes
        fixed = fixed.replaceAll("'([^']*)':", "\"$1\":");
        fixed = fixed.replaceAll(":[ ]*'([^']*)'", ": \"$1\"");
        
        // Fix trailing commas
        fixed = fixed.replaceAll(",\\s*}", "}");
        fixed = fixed.replaceAll(",\\s*]", "]");
        
        // Fix unquoted field names
        fixed = fixed.replaceAll("([{,]\\s*)([a-zA-Z_][a-zA-Z0-9_]*)(\\s*:)", "$1\"$2\"$3");
        
        return fixed;
    }

    /**
     * Fixes order ID to match required pattern.
     */
    private String fixOrderId(String orderId) {
        // Extract any numbers from the ID
        String numbers = orderId.replaceAll("[^0-9]", "");
        
        // Ensure we have exactly 6 digits
        if (numbers.length() >= 6) {
            numbers = numbers.substring(0, 6);
        } else {
            // Pad with zeros if needed
            numbers = String.format("%06d", numbers.isEmpty() ? 1 : Integer.parseInt(numbers));
        }
        
        return "ORD-" + numbers;
    }

    /**
     * Fixes email format issues.
     */
    private String fixEmail(String email) {
        String fixed = email.trim().toLowerCase();
        
        // Add missing @ symbol if there's none
        if (!fixed.contains("@")) {
            fixed = fixed.replaceFirst("\\s+", "@");
        }
        
        // Add .com if missing domain extension
        if (!fixed.matches(".*\\.[a-z]{2,}$")) {
            fixed += ".com";
        }
        
        // Remove any spaces
        fixed = fixed.replaceAll("\\s+", "");
        
        return fixed;
    }

    /**
     * Updates a field in the order using reflection.
     */
    private OrderRequest updateOrderField(OrderRequest order, String fieldName, Object newValue) {
        try {
            // Convert to JSON, update field, and convert back
            JsonNode node = objectMapper.valueToTree(order);
            ((ObjectNode) node).putPOJO(fieldName, newValue);
            return objectMapper.treeToValue(node, OrderRequest.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to update order field", e);
            return order;
        }
    }

    /**
     * Validates raw JSON string without parsing to a specific type.
     */
    public ValidationResult validateRawJson(String json) {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        List<ValidationResult.ValidationWarning> warnings = new ArrayList<>();
        List<ValidationResult.RecoveryAttempt> recoveryAttempts = new ArrayList<>();
        
        try {
            // Attempt to parse as JSON
            JsonNode node = objectMapper.readTree(json);
            
            // Check for common issues
            if (node.isObject() && node.size() == 0) {
                warnings.add(new ValidationResult.ValidationWarning(
                        "root",
                        "Empty JSON object",
                        "Consider if this is intentional"
                ));
            }
            
            // Validate structure
            validateJsonStructure(node, "", errors, warnings);
            
            return new ValidationResult(
                    errors.isEmpty(),
                    LocalDateTime.now(),
                    errors,
                    warnings,
                    recoveryAttempts,
                    node,
                    Map.of("type", "raw_json_validation")
            );
            
        } catch (JsonProcessingException e) {
            // Attempt recovery
            String fixed = fixCommonJsonIssues(json);
            if (!fixed.equals(json)) {
                recoveryAttempts.add(new ValidationResult.RecoveryAttempt(
                        "fix_json_syntax",
                        false,
                        "Attempted to fix JSON syntax errors",
                        fixed
                ));
                
                try {
                    JsonNode node = objectMapper.readTree(fixed);
                    recoveryAttempts.set(0, new ValidationResult.RecoveryAttempt(
                            "fix_json_syntax",
                            true,
                            "Successfully fixed JSON syntax",
                            node
                    ));
                    
                    return new ValidationResult(
                            true,
                            LocalDateTime.now(),
                            errors,
                            warnings,
                            recoveryAttempts,
                            node,
                            Map.of("recovered", true)
                    );
                } catch (JsonProcessingException e2) {
                    errors.add(new ValidationResult.ValidationError(
                            "json",
                            "Invalid JSON: " + e2.getMessage(),
                            json,
                            "valid_json"
                    ));
                }
            }
        }
        
        return new ValidationResult(
                false,
                LocalDateTime.now(),
                errors,
                warnings,
                recoveryAttempts,
                null,
                Map.of("stage", "json_parsing_failed")
        );
    }

    /**
     * Recursively validates JSON structure.
     */
    private void validateJsonStructure(JsonNode node, String path, 
                                     List<ValidationResult.ValidationError> errors,
                                     List<ValidationResult.ValidationWarning> warnings) {
        
        if (node.isNull()) {
            warnings.add(new ValidationResult.ValidationWarning(
                    path,
                    "Null value found",
                    "Consider if null is appropriate here"
            ));
        } else if (node.isArray()) {
            if (node.size() == 0) {
                warnings.add(new ValidationResult.ValidationWarning(
                        path,
                        "Empty array",
                        "Arrays should typically contain at least one element"
                ));
            }
            
            for (int i = 0; i < node.size(); i++) {
                validateJsonStructure(node.get(i), path + "[" + i + "]", errors, warnings);
            }
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldPath = path.isEmpty() ? field.getKey() : path + "." + field.getKey();
                
                // Check for problematic field names
                if (field.getKey().contains(" ")) {
                    errors.add(new ValidationResult.ValidationError(
                            fieldPath,
                            "Field name contains spaces",
                            field.getKey(),
                            "no_spaces_in_field_names"
                    ));
                }
                
                validateJsonStructure(field.getValue(), fieldPath, errors, warnings);
            }
        }
    }
}