package com.coherentsolutions.l3structuredoutput.s8.controllers;

import com.coherentsolutions.l3structuredoutput.s8.models.ResponseModels.ProductRecommendation;
import com.coherentsolutions.l3structuredoutput.s8.models.ResponseModels.SearchResults;
import com.coherentsolutions.l3structuredoutput.s8.models.ResponseModels.StockAnalysis;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * This controller demonstrates OpenAI's built-in JSON modes:
 * - JSON_OBJECT: Ensures output is valid JSON
 * - JSON_SCHEMA: Ensures output conforms to a provided JSON schema
 */
@RestController
@RequestMapping("/json-modes/openai")
public class OpenAiJsonModesController {

    private final ChatModel openAiChatModel;

    @Autowired
    public OpenAiJsonModesController(ChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    /**
     * Uses JSON_OBJECT mode to ensure valid JSON response
     */
    @GetMapping("/json-object")
    public String getProductRecommendationJsonObject(
            @RequestParam(defaultValue = "smartphone") String productType) {

        String jsonSchema = "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\",\"description\":\"Product name\"},\"category\":{\"type\":\"string\",\"description\":\"Product category\"},\"price\":{\"type\":\"number\",\"description\":\"Product price in USD\"},\"rating\":{\"type\":\"number\",\"description\":\"Product rating out of 5\"},\"features\":{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"minItems\":3,\"description\":\"List of product features\"},\"availability\":{\"type\":\"string\",\"description\":\"Product availability status\"}},\"required\":[\"name\",\"category\",\"price\",\"rating\",\"features\",\"availability\"]}";

        // Create options with JSON_OBJECT response format
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_OBJECT,jsonSchema))
                .build();

        String promptText = String.format(
                "Recommend a %s product with detailed features and specifications. " +
                        "Include name, category, price, rating (out of 5), at least 3 features, and availability.",
                productType);

        // Send request with JSON_OBJECT mode
        return openAiChatModel.call(new Prompt(promptText, options))
                .getResult()
                .getOutput()
                .getText();
    }

    /**
     * Uses JSON_SCHEMA mode to ensure response conforms to our schema
     */
    @GetMapping("/json-schema")
    public ProductRecommendation getProductRecommendationJsonSchema(
            @RequestParam(defaultValue = "laptop") String productType) {

        // Create a converter to get the JSON schema
        BeanOutputConverter<ProductRecommendation> converter =
                new BeanOutputConverter<>(ProductRecommendation.class);

        // Extract the schema from the format instructions
        String jsonSchema = converter.getJsonSchema();

        // Create options with JSON_SCHEMA response format
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                .build();

        String promptText = String.format(
                "Recommend a %s product with detailed features and specifications.",
                productType);

        // Send request with JSON_SCHEMA mode
        String responseContent = openAiChatModel.call(new Prompt(promptText, options))
                .getResult()
                .getOutput()
                .getText();

        // Parse the response content into our model
        return converter.convert(responseContent);
    }

    /**
     * Complex example using JSON_SCHEMA mode with nested objects
     */
    @GetMapping("/search")
    public SearchResults searchWithJsonSchema(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int numResults) {

        // Create a converter for our complex nested model
        BeanOutputConverter<SearchResults> converter =
                new BeanOutputConverter<>(SearchResults.class);

        // Extract the schema
        String jsonSchema = converter.getJsonSchema();

        // Create options with JSON_SCHEMA response format
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4o")  // Using more capable model for complex output
                .temperature(0.2) // Lower temperature for more consistent results
                .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                .build();

        String promptText = String.format(
                "Perform a search for '%s' and return %d results with relevant information. " +
                        "Make sure to include realistic URLs, relevance scores between 0 and 1, " +
                        "and appropriate summaries.",
                query, numResults);

        // Send request
        String responseContent = openAiChatModel.call(new Prompt(promptText, options))
                .getResult()
                .getOutput()
                .getText();

        // Parse the response
        return converter.convert(responseContent);
    }

    /**
     * Compare stock analysis with and without JSON schema
     */
    @PostMapping("/compare-json-modes")
    public ComparisonResult compareJsonModes(@RequestParam String stockSymbol) {
        String jsonSchema1 = "{\"type\":\"object\",\"properties\":{\"ticker\":{\"type\":\"string\",\"description\":\"Stock ticker symbol\"},\"currentPrice\":{\"type\":\"number\",\"description\":\"Current stock price in USD\"},\"peRatio\":{\"type\":\"number\",\"description\":\"Price to earnings ratio\"},\"marketCap\":{\"type\":\"number\",\"description\":\"Market capitalization in USD\"},\"dividendYield\":{\"type\":\"number\",\"description\":\"Annual dividend yield as a percentage\"},\"fiftyTwoWeekRange\":{\"type\":\"object\",\"properties\":{\"low\":{\"type\":\"number\",\"description\":\"52-week low price\"},\"high\":{\"type\":\"number\",\"description\":\"52-week high price\"}},\"required\":[\"low\",\"high\"]},\"analysis\":{\"type\":\"string\",\"description\":\"Brief analysis paragraph about the stock\"}},\"required\":[\"ticker\",\"currentPrice\",\"peRatio\",\"marketCap\",\"dividendYield\",\"fiftyTwoWeekRange\",\"analysis\"]}";

        // First, get response using JSON_OBJECT mode
        OpenAiChatOptions jsonObjectOptions = OpenAiChatOptions.builder()
                .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_OBJECT, jsonSchema1))
                .build();

        String jsonObjectPrompt = String.format(
                "Provide a detailed analysis of the %s stock, including current price, " +
                        "P/E ratio, market cap, dividend yield, 52-week range, and a brief analysis paragraph.",
                stockSymbol);

        String jsonObjectResponse = openAiChatModel.call(new Prompt(jsonObjectPrompt, jsonObjectOptions))
                .getResult()
                .getOutput()
                .getText();

        // Second, get response using JSON_SCHEMA mode
        BeanOutputConverter<StockAnalysis> converter = new BeanOutputConverter<>(StockAnalysis.class);
        String jsonSchema = converter.getJsonSchema();

        OpenAiChatOptions jsonSchemaOptions = OpenAiChatOptions.builder()
                .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                .build();

        String jsonSchemaPrompt = String.format(
                "Provide a detailed analysis of the %s stock.",
                stockSymbol);

        String jsonSchemaResponse = openAiChatModel.call(new Prompt(jsonSchemaPrompt, jsonSchemaOptions))
                .getResult()
                .getOutput()
                .getText();

        StockAnalysis parsedResponse = converter.convert(jsonSchemaResponse);

        // Return both responses for comparison
        return new ComparisonResult(
                jsonObjectResponse,
                jsonSchemaResponse,
                parsedResponse
        );
    }

    // Record for comparing different JSON mode responses
    record ComparisonResult(
            String jsonObjectResponse,
            String jsonSchemaResponse,
            StockAnalysis parsedStockAnalysis
    ) {}
}