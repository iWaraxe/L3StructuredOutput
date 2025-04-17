package com.coherentsolutions.l3structuredoutput.controllers.s6;

import com.coherentsolutions.l3structuredoutput.model.s6.TravelRequest;
import com.coherentsolutions.l3structuredoutput.services.s6.ActivityListService;
import com.coherentsolutions.l3structuredoutput.services.s6.DestinationMapService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller that exposes endpoints demonstrating different output converters.
 */
@RestController
@RequestMapping("/api/travel")
public class TravelController {

    private final DestinationMapService destinationMapService;
    private final ActivityListService activityListService;

    public TravelController(
            DestinationMapService destinationMapService,
            ActivityListService activityListService) {
        this.destinationMapService = destinationMapService;
        this.activityListService = activityListService;
    }

    /**
     * Recommends a single travel destination using MapOutputConverter.
     */
    @PostMapping("/destinations/recommend")
    public Map<String, Object> recommendDestination(@RequestBody TravelRequest request) {
        return destinationMapService.recommendDestination(request);
    }

    /**
     * Suggests multiple destinations using MapOutputConverter with nested structures.
     */
    @PostMapping("/destinations/suggest")
    public Map<String, Object> suggestDestinations(
            @RequestBody TravelRequest request,
            @RequestParam(defaultValue = "3") int count) {
        return destinationMapService.suggestMultipleDestinations(request, count);
    }

    /**
     * Suggests activities for a destination using ListOutputConverter.
     */
    @GetMapping("/activities")
    public List<String> suggestActivities(
            @RequestParam String destination,
            @RequestParam String travelStyle,
            @RequestParam(defaultValue = "5") int count) {
        return activityListService.suggestActivities(destination, travelStyle, count);
    }

    /**
     * Creates a daily itinerary using ListOutputConverter.
     */
    @GetMapping("/itinerary")
    public List<String> getDailyItinerary(
            @RequestParam String destination,
            @RequestParam String travelStyle) {
        return activityListService.suggestDailyItinerary(destination, travelStyle);
    }

    /**
     * Creates a packing list using ListOutputConverter.
     */
    @GetMapping("/packing-list")
    public List<String> getPackingList(
            @RequestParam String destination,
            @RequestParam String season) {
        return activityListService.createPackingList(destination, season);
    }
}