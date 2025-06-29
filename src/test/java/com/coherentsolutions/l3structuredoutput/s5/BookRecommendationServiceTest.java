package com.coherentsolutions.l3structuredoutput.s5;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class BookRecommendationServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private BookRecommendationService bookRecommendationService;

    @BeforeEach
    void setUp() {
        bookRecommendationService = new BookRecommendationService(chatModel);
    }

    @Test
    void getBookRecommendation_ShouldReturnValidBookRecommendation() {
        // Given
        String genre = "Science Fiction";
        String mood = "adventurous";
        String theme = "space exploration";

        String mockResponse = """
            {
              "title": "The Martian",
              "author": "Andy Weir",
              "genre": "Science Fiction",
              "publicationYear": 2011,
              "summary": "An astronaut becomes stranded on Mars and must use his ingenuity to survive while awaiting rescue."
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        BookRecommendation recommendation = bookRecommendationService.getBookRecommendation(genre, mood, theme);

        // Then
        assertNotNull(recommendation);
        assertEquals("The Martian", recommendation.title());
        assertEquals("Andy Weir", recommendation.author());
        assertEquals("Science Fiction", recommendation.genre());
        assertEquals(2011, recommendation.publicationYear());
        assertNotNull(recommendation.summary());
        assertTrue(recommendation.summary().contains("Mars"));
    }

    @Test
    void getBookRecommendation_WithDifferentGenres_ShouldReturnAppropriateRecommendations() {
        // Given
        String genre = "Mystery";
        String mood = "suspenseful";
        String theme = "detective investigation";

        String mockResponse = """
            {
              "title": "The Girl with the Dragon Tattoo",
              "author": "Stieg Larsson",
              "genre": "Mystery",
              "publicationYear": 2005,
              "summary": "A journalist and a hacker investigate a decades-old disappearance case that uncovers dark family secrets."
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        BookRecommendation recommendation = bookRecommendationService.getBookRecommendation(genre, mood, theme);

        // Then
        assertNotNull(recommendation);
        assertEquals("Mystery", recommendation.genre());
        assertEquals(2005, recommendation.publicationYear());
        assertNotNull(recommendation.title());
        assertNotNull(recommendation.author());
    }

    @Test
    void getBookRecommendation_WithEmptyParameters_ShouldStillReturnValidResponse() {
        // Given
        String genre = "";
        String mood = "";
        String theme = "";

        String mockResponse = """
            {
              "title": "To Kill a Mockingbird",
              "author": "Harper Lee",
              "genre": "Fiction",
              "publicationYear": 1960,
              "summary": "A classic novel about racial injustice and childhood innocence in the American South."
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        BookRecommendation recommendation = bookRecommendationService.getBookRecommendation(genre, mood, theme);

        // Then
        assertNotNull(recommendation);
        assertNotNull(recommendation.title());
        assertNotNull(recommendation.publicationYear());
    }
}