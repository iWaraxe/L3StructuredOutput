package com.coherentsolutions.l3structuredoutput.s5;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class AdvancedBookRecommendationServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private AdvancedBookRecommendationService advancedBookRecommendationService;

    @BeforeEach
    void setUp() {
        advancedBookRecommendationService = new AdvancedBookRecommendationService(chatModel);
    }

    @Test
    void getBookRecommendationsByMood_ShouldReturnMapWithMultipleMoods() {
        // Given
        String genre = "Fiction";
        List<String> moods = Arrays.asList("uplifting", "melancholic", "thrilling");

        String mockResponse = """
            {
              "uplifting": [
                {
                  "title": "The Alchemist",
                  "author": "Paulo Coelho",
                  "genre": "Fiction",
                  "publicationYear": 1988,
                  "summary": "A young shepherd's journey to find a treasure leads to self-discovery."
                },
                {
                  "title": "Life of Pi",
                  "author": "Yann Martel",
                  "genre": "Fiction",
                  "publicationYear": 2001,
                  "summary": "A boy survives a shipwreck and shares a lifeboat with a Bengal tiger."
                }
              ],
              "melancholic": [
                {
                  "title": "The Book Thief",
                  "author": "Markus Zusak",
                  "genre": "Fiction",
                  "publicationYear": 2005,
                  "summary": "Death narrates the story of a girl living in Nazi Germany who steals books."
                },
                {
                  "title": "Never Let Me Go",
                  "author": "Kazuo Ishiguro",
                  "genre": "Fiction",
                  "publicationYear": 2005,
                  "summary": "Students at a boarding school discover their dark purpose in life."
                }
              ],
              "thrilling": [
                {
                  "title": "Gone Girl",
                  "author": "Gillian Flynn",
                  "genre": "Fiction",
                  "publicationYear": 2012,
                  "summary": "A woman's disappearance reveals dark secrets about a marriage."
                },
                {
                  "title": "The Da Vinci Code",
                  "author": "Dan Brown",
                  "genre": "Fiction",
                  "publicationYear": 2003,
                  "summary": "A symbologist uncovers a religious mystery while on the run from a secret society."
                }
              ]
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, List<BookRecommendation>> recommendations = 
                advancedBookRecommendationService.getBookRecommendationsByMood(genre, moods);

        // Then
        assertNotNull(recommendations);
        assertEquals(3, recommendations.size());
        assertTrue(recommendations.containsKey("uplifting"));
        assertTrue(recommendations.containsKey("melancholic"));
        assertTrue(recommendations.containsKey("thrilling"));

        // Verify uplifting books
        List<BookRecommendation> upliftingBooks = recommendations.get("uplifting");
        assertEquals(2, upliftingBooks.size());
        assertEquals("The Alchemist", upliftingBooks.get(0).title());
        assertEquals(1988, upliftingBooks.get(0).publicationYear());

        // Verify melancholic books
        List<BookRecommendation> melancholicBooks = recommendations.get("melancholic");
        assertEquals(2, melancholicBooks.size());
        assertTrue(melancholicBooks.stream().anyMatch(book -> book.title().equals("The Book Thief")));

        // Verify thrilling books
        List<BookRecommendation> thrillingBooks = recommendations.get("thrilling");
        assertEquals(2, thrillingBooks.size());
        assertTrue(thrillingBooks.stream().allMatch(book -> book.publicationYear() != null));
    }

    @Test
    void getBookRecommendationsByMood_WithSingleMood_ShouldReturnValidMap() {
        // Given
        String genre = "Mystery";
        List<String> moods = List.of("suspenseful");

        String mockResponse = """
            {
              "suspenseful": [
                {
                  "title": "The Silent Patient",
                  "author": "Alex Michaelides",
                  "genre": "Mystery",
                  "publicationYear": 2019,
                  "summary": "A psychotherapist becomes obsessed with a woman who refuses to speak after allegedly killing her husband."
                },
                {
                  "title": "Big Little Lies",
                  "author": "Liane Moriarty",
                  "genre": "Mystery",
                  "publicationYear": 2014,
                  "summary": "Three women's lives intertwine leading up to a shocking event at a school trivia night."
                }
              ]
            }
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, List<BookRecommendation>> recommendations = 
                advancedBookRecommendationService.getBookRecommendationsByMood(genre, moods);

        // Then
        assertNotNull(recommendations);
        assertEquals(1, recommendations.size());
        assertTrue(recommendations.containsKey("suspenseful"));
        assertEquals(2, recommendations.get("suspenseful").size());
    }

    @Test
    void getBookRecommendationsByMood_ShouldHandleEmptyMoodsList() {
        // Given
        String genre = "Romance";
        List<String> moods = List.of();

        String mockResponse = """
            {}
            """;

        ChatResponse chatResponse = createMockResponse(mockResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // When
        Map<String, List<BookRecommendation>> recommendations = 
                advancedBookRecommendationService.getBookRecommendationsByMood(genre, moods);

        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }
}