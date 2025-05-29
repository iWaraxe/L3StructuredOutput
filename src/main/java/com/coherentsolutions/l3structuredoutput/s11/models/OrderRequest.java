package com.coherentsolutions.l3structuredoutput.s11.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Example model with Jakarta Bean Validation annotations.
 * Demonstrates how to validate AI-generated structured output.
 */
@JsonPropertyOrder({"orderId", "customerEmail", "orderDate", "items", "totalAmount", "shippingAddress", "paymentMethod"})
public record OrderRequest(
        @JsonPropertyDescription("Unique order identifier")
        @NotBlank(message = "Order ID is required")
        @Pattern(regexp = "^ORD-\\d{6}$", message = "Order ID must match pattern ORD-XXXXXX")
        String orderId,

        @JsonPropertyDescription("Customer email address")
        @NotBlank(message = "Customer email is required")
        @Email(message = "Must be a valid email address")
        String customerEmail,

        @JsonPropertyDescription("Date when order was placed")
        @NotNull(message = "Order date is required")
        @PastOrPresent(message = "Order date cannot be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate orderDate,

        @JsonPropertyDescription("List of items in the order")
        @NotEmpty(message = "Order must contain at least one item")
        @Size(max = 50, message = "Order cannot contain more than 50 items")
        List<OrderItem> items,

        @JsonPropertyDescription("Total amount for the order in USD")
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Total amount must be at least $0.01")
        @DecimalMax(value = "999999.99", message = "Total amount cannot exceed $999,999.99")
        Double totalAmount,

        @JsonPropertyDescription("Shipping address for the order")
        @NotNull(message = "Shipping address is required")
        ShippingAddress shippingAddress,

        @JsonPropertyDescription("Payment method used")
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
    
    @JsonPropertyOrder({"productId", "productName", "quantity", "unitPrice"})
    public record OrderItem(
            @JsonPropertyDescription("Product identifier")
            @NotBlank(message = "Product ID is required")
            String productId,

            @JsonPropertyDescription("Product name")
            @NotBlank(message = "Product name is required")
            @Size(max = 100, message = "Product name too long")
            String productName,

            @JsonPropertyDescription("Quantity ordered")
            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 1000, message = "Quantity cannot exceed 1000")
            Integer quantity,

            @JsonPropertyDescription("Price per unit in USD")
            @NotNull(message = "Unit price is required")
            @DecimalMin(value = "0.01", message = "Unit price must be positive")
            Double unitPrice
    ) {}

    @JsonPropertyOrder({"street", "city", "state", "zipCode", "country"})
    public record ShippingAddress(
            @JsonPropertyDescription("Street address")
            @NotBlank(message = "Street is required")
            String street,

            @JsonPropertyDescription("City name")
            @NotBlank(message = "City is required")
            String city,

            @JsonPropertyDescription("State or province")
            @NotBlank(message = "State is required")
            @Size(min = 2, max = 2, message = "State must be 2-letter code")
            String state,

            @JsonPropertyDescription("ZIP or postal code")
            @NotBlank(message = "ZIP code is required")
            @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid ZIP code format")
            String zipCode,

            @JsonPropertyDescription("Country code")
            @NotBlank(message = "Country is required")
            @Size(min = 2, max = 2, message = "Country must be 2-letter ISO code")
            String country
    ) {}

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, CRYPTOCURRENCY
    }
}