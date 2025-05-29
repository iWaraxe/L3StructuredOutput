package com.coherentsolutions.l3structuredoutput.s5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeanConverterDemoController.class)
class BeanConverterDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRecommendationService bookService;

    @MockBean
    private AdvancedBookRecommendationService advancedBookService;

    @MockBean
    private CapitalInfoService capitalInfoService;

    @Test
    void getBookRecommendation_ShouldReturnBookRecommendation() throws Exception {
        // Given
        BookRecommendation mockRecommendation = new BookRecommendation(
                "1984",
                "George Orwell",
                "Dystopian Fiction",
                1949,
                "A totalitarian regime controls every aspect of life in a dystopian future."
        );

        when(bookService.getBookRecommendation("dystopian", "dark", "totalitarianism"))
                .thenReturn(mockRecommendation);

        // When & Then
        mockMvc.perform(get("/api/books/recommend")
                        .param("genre", "dystopian")
                        .param("mood", "dark")
                        .param("theme", "totalitarianism"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.author").value("George Orwell"))
                .andExpect(jsonPath("$.genre").value("Dystopian Fiction"))
                .andExpect(jsonPath("$.publicationYear").value(1949))
                .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    void getBooksByMood_ShouldReturnMapOfBooksByMood() throws Exception {
        // Given
        Map<String, List<BookRecommendation>> mockRecommendations = new HashMap<>();
        
        List<BookRecommendation> happyBooks = Arrays.asList(
                new BookRecommendation("The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Comedy", 1979, "A humorous sci-fi adventure."),
                new BookRecommendation("Good Omens", "Terry Pratchett & Neil Gaiman", "Comedy", 1990, "An angel and demon team up.")
        );
        
        List<BookRecommendation> darkBooks = Arrays.asList(
                new BookRecommendation("The Road", "Cormac McCarthy", "Post-apocalyptic", 2006, "A father and son survive in a post-apocalyptic world.")
        );
        
        mockRecommendations.put("happy", happyBooks);
        mockRecommendations.put("dark", darkBooks);

        when(advancedBookService.getBookRecommendationsByMood(eq("comedy"), any()))
                .thenReturn(mockRecommendations);

        // When & Then
        mockMvc.perform(get("/api/books/recommend-by-mood")
                        .param("genre", "comedy")
                        .param("moods", "happy", "dark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.happy").isArray())
                .andExpect(jsonPath("$.happy[0].title").value("The Hitchhiker's Guide to the Galaxy"))
                .andExpect(jsonPath("$.dark").isArray())
                .andExpect(jsonPath("$.dark[0].title").value("The Road"));
    }

    @Test
    void getCapitalInfo_ShouldReturnCapitalInformation() throws Exception {
        // Given
        CapitalInfo mockCapitalInfo = new CapitalInfo(
                "London",
                9.0,
                "Greater London",
                "English",
                "Pound Sterling",
                new String[]{"Big Ben", "Tower Bridge", "Buckingham Palace"}
        );

        when(capitalInfoService.getCapitalInfo("UK")).thenReturn(mockCapitalInfo);

        // When & Then
        mockMvc.perform(get("/api/capitals/UK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("London"))
                .andExpect(jsonPath("$.population").value(9.0))
                .andExpect(jsonPath("$.region").value("Greater London"))
                .andExpect(jsonPath("$.language").value("English"))
                .andExpect(jsonPath("$.currency").value("Pound Sterling"))
                .andExpect(jsonPath("$.landmarks").isArray())
                .andExpect(jsonPath("$.landmarks[0]").value("Big Ben"));
    }

    @Test
    void compareCapitals_ShouldReturnMapOfCapitals() throws Exception {
        // Given
        Map<String, CapitalInfo> mockCapitals = new HashMap<>();
        
        mockCapitals.put("France", new CapitalInfo(
                "Paris", 2.2, "Île-de-France", "French", "Euro",
                new String[]{"Eiffel Tower", "Louvre"}
        ));
        
        mockCapitals.put("Spain", new CapitalInfo(
                "Madrid", 3.3, "Community of Madrid", "Spanish", "Euro",
                new String[]{"Royal Palace", "Prado Museum"}
        ));

        when(capitalInfoService.compareCapitals(any())).thenReturn(mockCapitals);

        // When & Then
        mockMvc.perform(get("/api/capitals/compare")
                        .param("countries", "France", "Spain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.France.city").value("Paris"))
                .andExpect(jsonPath("$.France.population").value(2.2))
                .andExpect(jsonPath("$.Spain.city").value("Madrid"))
                .andExpect(jsonPath("$.Spain.population").value(3.3));
    }

    @Test
    void getBookRecommendation_WithMissingParameters_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/books/recommend")
                        .param("genre", "fiction")
                        .param("mood", "happy"))
                // Missing "theme" parameter
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooksByMood_WithEmptyMoods_ShouldStillWork() throws Exception {
        // Given
        when(advancedBookService.getBookRecommendationsByMood(eq("fiction"), any()))
                .thenReturn(new HashMap<>());

        // When & Then
        mockMvc.perform(get("/api/books/recommend-by-mood")
                        .param("genre", "fiction")
                        .param("moods", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }
}