package com.coherentsolutions.l3structuredoutput.s3;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for movie recommendations.
 */
@RestController
@RequestMapping("/api/movies")
public class MovieRecommendationController {

    private final MovieRecommendationService recommendationService;

    public MovieRecommendationController(MovieRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommend")
    public MovieRecommendation getRecommendation(@RequestBody MoviePreferenceRequest request) {
        return recommendationService.getRecommendation(request);
    }

    @PostMapping("/recommend/multiple")
    public List<MovieRecommendation> getRecommendations(@RequestBody MoviePreferenceRequest request) {
        return recommendationService.getRecommendations(request);
    }
}