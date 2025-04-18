package com.coherentsolutions.l3structuredoutput.s2;

import java.util.List;

/**
 * Record representing a request for a recipe generation.
 * Contains all parameters needed for recipe customization.
 */
public record RecipeRequest(
        String cuisine,
        String dishType,
        List<String> ingredients,
        String dietaryRestrictions
) {
}