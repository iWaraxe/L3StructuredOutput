package com.coherentsolutions.l3structuredoutput.s5;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BookRecommendationService {

    private final OpenAiChatModel chatModel;

    public BookRecommendationService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Gets a book recommendation based on user preferences.
     */
    public BookRecommendation getBookRecommendation(String genre, String mood, String theme) {
        // Step 1: Create the BeanOutputConverter for BookRecommendation
        BeanOutputConverter<BookRecommendation> converter =
                new BeanOutputConverter<>(BookRecommendation.class);

        // Step 2: Get the format instructions
        String format = converter.getFormat();

        // Step 3: Create a prompt template with the user's preferences
        String promptText = """
            Recommend a {genre} book with a {mood} mood that explores the theme of {theme}.
            Provide a recommendation with title, author, genre, publication year, and a brief summary.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);

        // Step 4: Render the template with parameters
        String renderedPrompt = template.render(Map.of(
                "genre", genre,
                "mood", mood,
                "theme", theme,
                "format", format
        ));

        // Step 5: Create a prompt and call the AI model
        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        // Step 6: Convert the response text to a BookRecommendation object
        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }
}