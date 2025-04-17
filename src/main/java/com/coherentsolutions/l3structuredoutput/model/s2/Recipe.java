package com.coherentsolutions.l3structuredoutput.model.s2;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Record representing a recipe response from the AI model.
 * Structured to capture recipe details in an organized format.
 */
@JsonPropertyOrder({"name", "cuisine", "preparationTime", "cookingTime", "difficulty", "servingSize", "ingredients", "instructions", "nutritionalInfo", "servingSuggestions"})
public record Recipe(
        @JsonPropertyDescription("The name of the dish")
        String name,

        @JsonPropertyDescription("The cuisine type (Italian, Mexican, etc.)")
        String cuisine,

        @JsonPropertyDescription("Preparation time in minutes")
        Integer preparationTime,

        @JsonPropertyDescription("Cooking time in minutes")
        Integer cookingTime,

        @JsonPropertyDescription("Difficulty level (Easy, Medium, Hard)")
        String difficulty,

        @JsonPropertyDescription("Number of servings the recipe yields")
        Integer servingSize,

        @JsonPropertyDescription("List of ingredients with measurements")
        List<Ingredient> ingredients,

        @JsonPropertyDescription("Step-by-step cooking instructions")
        List<String> instructions,

        @JsonPropertyDescription("Nutritional information per serving")
        NutritionalInfo nutritionalInfo,

        @JsonPropertyDescription("Suggestions for serving the dish")
        List<String> servingSuggestions
) {
    /**
     * Nested record for representing ingredients with measurements
     */
    public record Ingredient(
            @JsonPropertyDescription("Name of the ingredient")
            String name,

            @JsonPropertyDescription("Quantity of the ingredient")
            String quantity,

            @JsonPropertyDescription("Unit of measurement (g, ml, cup, etc.)")
            String unit
    ) {}

    /**
     * Nested record for representing nutritional information
     */
    public record NutritionalInfo(
            @JsonPropertyDescription("Calories per serving")
            Integer calories,

            @JsonPropertyDescription("Protein in grams")
            Double protein,

            @JsonPropertyDescription("Carbohydrates in grams")
            Double carbohydrates,

            @JsonPropertyDescription("Fat in grams")
            Double fat
    ) {}
}