package com.coherentsolutions.l3structuredoutput.s3;

import com.coherentsolutions.l3structuredoutput.BaseStructuredOutputTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieRecommendationServiceTest extends BaseStructuredOutputTest {

    @Mock
    private OpenAiChatModel chatModel;

    private MovieRecommendationService movieRecommendationService;

    @BeforeEach
    void setUp() {
        movieRecommendationService = new MovieRecommendationService(chatModel);
    }

    @Test
    void getRecommendation_shouldReturnSingleMovieRecommendation() {
        // Given
        MoviePreferenceRequest request = new MoviePreferenceRequest(
            "sci-fi", 2010, "thoughtful", 1
        );

        String jsonResponse = """
            {
                "title": "Inception",
                "director": "Christopher Nolan",
                "year": 2010,
                "genre": "Sci-Fi",
                "rating": 8.8,
                "summary": "A mind-bending thriller about dream infiltration",
                "streamingPlatforms": ["Netflix", "Amazon Prime"]
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        MovieRecommendation result = movieRecommendationService.getRecommendation(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Inception");
        assertThat(result.director()).isEqualTo("Christopher Nolan");
        assertThat(result.year()).isEqualTo(2010);
        assertThat(result.genre()).isEqualTo("Sci-Fi");
        assertThat(result.rating()).isEqualTo(8.8);
        assertThat(result.summary()).contains("mind-bending");

        // Verify prompt contains request parameters
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        assertThat(promptContains(capturedPrompt, "sci-fi", "2010", "thoughtful")).isTrue();
    }

    @Test
    void getRecommendations_shouldReturnMultipleMovieRecommendations() {
        // Given
        MoviePreferenceRequest request = new MoviePreferenceRequest(
            "action", 2020, "exciting", 3
        );

        String jsonResponse = """
            [
                {
                    "title": "Tenet",
                    "director": "Christopher Nolan",
                    "year": 2020,
                    "genre": "Action",
                    "rating": 7.4,
                    "summary": "Time-inversion action thriller",
                    "streamingPlatforms": ["HBO Max"]
                },
                {
                    "title": "The Old Guard",
                    "director": "Gina Prince-Bythewood",
                    "year": 2020,
                    "genre": "Action",
                    "rating": 6.6,
                    "summary": "Immortal warriors fight through centuries",
                    "streamingPlatforms": ["Netflix"]
                },
                {
                    "title": "Extraction",
                    "director": "Sam Hargrave",
                    "year": 2020,
                    "genre": "Action",
                    "rating": 6.7,
                    "summary": "Mercenary rescue mission in Bangladesh",
                    "streamingPlatforms": ["Netflix"]
                }
            ]
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        List<MovieRecommendation> results = movieRecommendationService.getRecommendations(request);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(MovieRecommendation::title)
            .containsExactly("Tenet", "The Old Guard", "Extraction");
        assertThat(results).allMatch(movie -> movie.year() >= 2020);
        assertThat(results).allMatch(movie -> movie.genre().equals("Action"));

        // Verify prompt contains maxResults parameter
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        assertThat(promptContains(capturedPrompt, "3", "action", "2020", "exciting")).isTrue();
    }

    @Test
    void getRecommendation_shouldIncludeStructuredOutputFormat() {
        // Given
        MoviePreferenceRequest request = new MoviePreferenceRequest(
            "drama", 2015, "emotional", 1
        );

        ChatResponse mockResponse = createMockResponse("{}");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        movieRecommendationService.getRecommendation(request);

        // Then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        
        // Verify format instructions are included
        String promptText = capturedPrompt.getInstructions().get(0).getText();
        assertThat(promptText).contains("JSON");
        assertThat(promptText).contains("schema");
    }

    @Test
    void getRecommendations_withZeroMaxResults_shouldStillWork() {
        // Given
        MoviePreferenceRequest request = new MoviePreferenceRequest(
            "comedy", 2022, "funny", 0
        );

        String jsonResponse = "[]";
        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        List<MovieRecommendation> results = movieRecommendationService.getRecommendations(request);

        // Then
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void getRecommendation_withSpecialCharactersInMood_shouldHandleCorrectly() {
        // Given
        MoviePreferenceRequest request = new MoviePreferenceRequest(
            "horror", 2023, "spine-chilling & terrifying", 1
        );

        String jsonResponse = """
            {
                "title": "The Haunting",
                "director": "John Doe",
                "year": 2023,
                "genre": "Horror",
                "rating": 7.5,
                "summary": "A terrifying tale",
                "streamingPlatforms": ["Amazon Prime"]
            }
            """;

        ChatResponse mockResponse = createMockResponse(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        MovieRecommendation result = movieRecommendationService.getRecommendation(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.summary()).contains("terrifying");

        // Verify special characters are handled in prompt
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        assertThat(promptContains(capturedPrompt, "spine-chilling & terrifying")).isTrue();
    }
}