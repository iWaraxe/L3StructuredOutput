package com.coherentsolutions.l3structuredoutput.s2.ex2;

import java.util.List;

/**
 * Record representing a request for a recipe generation.
 * Contains all parameters needed for recipe customization.
 */
public record RecipeRequest(
        String cuisine,
        String dishType,
        List<String> ingredients,
        List<String> dietaryRestrictions,
        Integer cookingTime,
        Integer servings,
        String difficulty
) {
}