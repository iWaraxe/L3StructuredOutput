package com.coherentsolutions.l3structuredoutput.s12.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Invoice model using custom types that require custom conversion.
 */
@JsonPropertyOrder({"invoiceNumber", "issueDate", "dueDate", "customer", "items", "subtotal", "tax", "total"})
public record Invoice(
        String invoiceNumber,
        CustomDate issueDate,
        CustomDate dueDate,
        Customer customer,
        List<InvoiceItem> items,
        Money subtotal,
        Money tax,
        Money total
) {
    public record Customer(
            String name,
            String email,
            String address,
            String taxId
    ) {}
    
    public record InvoiceItem(
            String description,
            int quantity,
            Money unitPrice,
            Money total
    ) {}
}