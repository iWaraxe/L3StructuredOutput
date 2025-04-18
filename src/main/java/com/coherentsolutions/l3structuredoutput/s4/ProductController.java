package com.coherentsolutions.l3structuredoutput.s4;

import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductReview;
import com.coherentsolutions.l3structuredoutput.s4.ConverterFactory.ProductSummary;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for demonstrating various structured output converters.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductAIService productAIService;

    public ProductController(ProductAIService productAIService) {
        this.productAIService = productAIService;
    }

    @GetMapping("/generate")
    public Product generateProduct(
            @RequestParam String category,
            @RequestParam String priceRange) {
        return productAIService.generateProduct(category, priceRange);
    }

    @GetMapping("/generate/list")
    public List<Product> generateProductList(
            @RequestParam String category,
            @RequestParam(defaultValue = "3") int count) {
        return productAIService.generateProductList(category, count);
    }

    @GetMapping("/generate/summary")
    public ProductSummary generateProductSummary(
            @RequestParam String productType) {
        return productAIService.generateProductSummary(productType);
    }

    @GetMapping("/generate/review")
    public ProductReview generateProductReview(
            @RequestParam String productId,
            @RequestParam(defaultValue = "positive") String sentiment) {
        return productAIService.generateProductReview(productId, sentiment);
    }

    @GetMapping("/generate/features")
    public List<String> generateProductFeatures(
            @RequestParam String productType,
            @RequestParam(defaultValue = "5") int featureCount) {
        return productAIService.generateProductFeatures(productType, featureCount);
    }

    @GetMapping("/generate/properties")
    public Map<String, Object> generateProductProperties(
            @RequestParam String productType) {
        return productAIService.generateProductProperties(productType);
    }
}