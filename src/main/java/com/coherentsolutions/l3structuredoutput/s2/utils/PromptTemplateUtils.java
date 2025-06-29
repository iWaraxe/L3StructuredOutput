package com.coherentsolutions.l3structuredoutput.s2.utils;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class for working with prompt templates.
 * Provides methods for loading templates from various sources
 * and creating PromptTemplate objects.
 */
public class PromptTemplateUtils {

    /**
     * Creates a PromptTemplate from a Resource object.
     * Useful for templates loaded via @Value annotation.
     *
     * @param resource The Spring Resource containing the template
     * @return A PromptTemplate initialized with the resource content
     */
    public static PromptTemplate fromResource(Resource resource) {
        try {
            String templateContent = resourceToString(resource);
            return new PromptTemplate(templateContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load template from resource: " + resource.getFilename(), e);
        }
    }

    /**
     * Creates a PromptTemplate from a string template.
     * Useful for inline templates defined in code.
     *
     * @param template The template string with placeholders
     * @return A PromptTemplate initialized with the string
     */
    public static PromptTemplate fromString(String template) {
        return new PromptTemplate(template);
    }

    /**
     * Creates a rendered prompt string from a Resource template and parameters.
     * This is a convenience method that loads the template and renders it in one step.
     *
     * @param resource The Spring Resource containing the template
     * @param parameters Map of parameter names to values for substitution
     * @return The rendered prompt string with parameters substituted
     * @throws IOException If the resource cannot be read
     */
    public static String renderFromResource(Resource resource, Map<String, Object> parameters) throws IOException {
        PromptTemplate template = fromResource(resource);
        return template.render(parameters);
    }

    /**
     * Utility method to convert a Resource to a String.
     *
     * @param resource The Spring Resource to read
     * @return The resource content as a String
     * @throws IOException If the resource cannot be read
     */
    private static String resourceToString(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}