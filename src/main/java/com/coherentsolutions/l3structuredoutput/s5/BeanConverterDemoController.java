package com.coherentsolutions.l3structuredoutput.s5;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BeanConverterDemoController {

    private final BookRecommendationService bookService;
    private final AdvancedBookRecommendationService advancedBookService;
    private final CapitalInfoService capitalInfoService;

    public BeanConverterDemoController(
            BookRecommendationService bookService,
            AdvancedBookRecommendationService advancedBookService,
            CapitalInfoService capitalInfoService) {
        this.bookService = bookService;
        this.advancedBookService = advancedBookService;
        this.capitalInfoService = capitalInfoService;
    }

    @GetMapping("/books/recommend")
    public BookRecommendation getBookRecommendation(
            @RequestParam String genre,
            @RequestParam String mood,
            @RequestParam String theme) {
        return bookService.getBookRecommendation(genre, mood, theme);
    }

    @GetMapping("/books/recommend-by-mood")
    public Map<String, List<BookRecommendation>> getBooksByMood(
            @RequestParam String genre,
            @RequestParam List<String> moods) {
        return advancedBookService.getBookRecommendationsByMood(genre, moods);
    }

    @GetMapping("/capitals/{country}")
    public CapitalInfo getCapitalInfo(@PathVariable String country) {
        return capitalInfoService.getCapitalInfo(country);
    }

    @GetMapping("/capitals/compare")
    public Map<String, CapitalInfo> compareCapitals(@RequestParam List<String> countries) {
        return capitalInfoService.compareCapitals(countries);
    }
}