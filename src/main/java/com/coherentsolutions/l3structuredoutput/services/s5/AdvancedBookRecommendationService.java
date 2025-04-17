package com.coherentsolutions.l3structuredoutput.services.s5;

import com.coherentsolutions.l3structuredoutput.model.s5.BookRecommendation;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdvancedBookRecommendationService {

    private final OpenAiChatModel chatModel;

    public AdvancedBookRecommendationService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Gets book recommendations categorized by mood.
     */
    public Map<String, List<BookRecommendation>> getBookRecommendationsByMood(String genre, List<String> moods) {
        // Create a converter for Map<String, List<BookRecommendation>>
        BeanOutputConverter<Map<String, List<BookRecommendation>>> converter =
                new BeanOutputConverter<>(
                        new ParameterizedTypeReference<Map<String, List<BookRecommendation>>>() {}
                );

        String format = converter.getFormat();

        // Join the moods list into a comma-separated string
        String moodsList = String.join(", ", moods);

        String promptText = """
    Recommend {genre} books categorized by different moods: {moods}.
    
    For each mood, provide 2-3 book recommendations with ALL of the following details:
    - Title of the book
    - Author's full name
    - Genre (should match {genre})
    - Publication year as a 4-digit integer number (e.g., 1997, 2005, 2022) - THIS IS REQUIRED
    - A brief summary of the book (1-2 sentences)
    
    Make sure to include the publication year as a number, not as text or null.
    If you don't know the exact year, provide your best estimate (e.g., 2015 instead of null).
    
    Format the response as a map where each mood is a key and 
    the value is a list of book recommendations with all fields completed.
    {format}
    """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "genre", genre,
                "moods", moodsList,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }
}