package com.coherentsolutions.l3structuredoutput.s9.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.time.LocalDate;
import java.util.List;

/**
 * Demonstrates complex nested structures with property ordering for e-commerce.
 * Shows how annotations help generate better structured AI responses.
 */
@JsonRootName("product_catalog")
@JsonPropertyOrder({"catalogId", "catalogName", "lastUpdated", "totalProducts", "categories", "featuredProducts"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ProductCatalog(
        @JsonPropertyDescription("Unique identifier for the product catalog")
        @JsonProperty("catalog_id")
        String catalogId,

        @JsonPropertyDescription("Name of the product catalog")
        @JsonProperty("catalog_name")
        String catalogName,

        @JsonPropertyDescription("Date when the catalog was last updated")
        @JsonProperty("last_updated")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate lastUpdated,

        @JsonPropertyDescription("Total number of products in the catalog")
        @JsonProperty("total_products")
        Integer totalProducts,

        @JsonPropertyDescription("List of product categories available")
        List<Category> categories,

        @JsonPropertyDescription("Featured products for promotion")
        @JsonProperty("featured_products")
        List<FeaturedProduct> featuredProducts
) {
    
    @JsonPropertyOrder({"categoryId", "categoryName", "description", "productCount"})
    public record Category(
            @JsonPropertyDescription("Unique identifier for the category")
            @JsonProperty("category_id")
            String categoryId,

            @JsonPropertyDescription("Name of the category")
            @JsonProperty("category_name")
            String categoryName,

            @JsonPropertyDescription("Description of what products are in this category")
            String description,

            @JsonPropertyDescription("Number of products in this category")
            @JsonProperty("product_count")
            Integer productCount
    ) {}

    @JsonPropertyOrder({"productId", "productName", "price", "discount", "rating", "availability"})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FeaturedProduct(
            @JsonPropertyDescription("Unique product identifier")
            @JsonProperty("product_id")
            String productId,

            @JsonPropertyDescription("Name of the product")
            @JsonProperty("product_name")
            String productName,

            @JsonPropertyDescription("Current price in USD")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#,##0.00")
            Double price,

            @JsonPropertyDescription("Discount percentage if applicable")
            @JsonInclude(JsonInclude.Include.NON_DEFAULT)
            Integer discount,

            @JsonPropertyDescription("Average customer rating out of 5")
            Double rating,

            @JsonPropertyDescription("Stock availability status")
            Availability availability
    ) {}

    public enum Availability {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK, PRE_ORDER
    }
}