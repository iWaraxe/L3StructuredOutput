package com.coherentsolutions.l3structuredoutput.s3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieRecommendationController.class)
class MovieRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieRecommendationService movieRecommendationService;

    @Test
    void getSingleRecommendation_shouldReturnMovieRecommendation() throws Exception {
        // Given
        MovieRecommendation recommendation = new MovieRecommendation(
            "The Matrix",
            1999,
            "The Wachowskis",
            "Sci-Fi",
            8.7,
            "A hacker discovers reality is a simulation",
            List.of("Netflix", "Amazon Prime")
        );

        when(movieRecommendationService.getRecommendation(any(MoviePreferenceRequest.class)))
            .thenReturn(recommendation);

        String requestJson = """
            {
                "genre": "sci-fi",
                "releaseYearAfter": 1995,
                "mood": "thoughtful",
                "maxResults": 1
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/movies/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("The Matrix"))
            .andExpect(jsonPath("$.director").value("The Wachowskis"))
            .andExpect(jsonPath("$.year").value(1999))
            .andExpect(jsonPath("$.genre").value("Sci-Fi"))
            .andExpect(jsonPath("$.rating").value(8.7));
    }

    @Test
    void getMultipleRecommendations_shouldReturnMovieList() throws Exception {
        // Given
        List<MovieRecommendation> recommendations = List.of(
            new MovieRecommendation(
                "Blade Runner 2049",
                2017,
                "Denis Villeneuve",
                "Sci-Fi",
                8.0,
                "A young blade runner discovers a secret",
                List.of("HBO Max")
            ),
            new MovieRecommendation(
                "Arrival",
                2016,
                "Denis Villeneuve",
                "Sci-Fi",
                7.9,
                "Linguist communicates with aliens",
                List.of("Netflix", "Hulu")
            )
        );

        when(movieRecommendationService.getRecommendations(any(MoviePreferenceRequest.class)))
            .thenReturn(recommendations);

        String requestJson = """
            {
                "genre": "sci-fi",
                "releaseYearAfter": 2015,
                "mood": "thoughtful",
                "maxResults": 2
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/movies/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].title").value("Blade Runner 2049"))
            .andExpect(jsonPath("$[1].title").value("Arrival"))
            .andExpect(jsonPath("$[0].director").value("Denis Villeneuve"))
            .andExpect(jsonPath("$[1].director").value("Denis Villeneuve"));
    }

    @Test
    void getSingleRecommendation_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidRequestJson = """
            {
                "genre": "sci-fi"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/movies/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getRecommendations_withEmptyGenre_shouldReturnBadRequest() throws Exception {
        // Given
        String requestJson = """
            {
                "genre": "",
                "releaseYearAfter": 2020,
                "mood": "exciting",
                "maxResults": 5
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/movies/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getRecommendations_withNegativeYear_shouldReturnBadRequest() throws Exception {
        // Given
        String requestJson = """
            {
                "genre": "comedy",
                "releaseYearAfter": -2020,
                "mood": "funny",
                "maxResults": 3
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/movies/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }
}