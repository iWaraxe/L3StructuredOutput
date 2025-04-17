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
            For each mood, provide 2-3 book recommendations with:
            - Title
            - Author
            - Genre
            - Publication year (numerical value, exact year, e.g. 1997 - this is REQUIRED)
            - A brief summary
            
            All fields must be included for each book. If you don't know the exact publication year, provide your best estimate.            
            
            Return the recommendations as a map where each key is a mood and 
            the value is a list of book recommendations.
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