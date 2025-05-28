package com.coherentsolutions.l3structuredoutput.s7;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherChatClientController.class)
class WeatherChatClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void getWeather_shouldReturnWeatherInfo() throws Exception {
        // Given
        String city = "Paris";
        WeatherService.WeatherInfo weatherInfo = new WeatherService.WeatherInfo(
            city,
            "Partly cloudy",
            19.5,
            70,
            "Scattered clouds throughout the day"
        );
        
        when(weatherService.getWeatherWithChatClient(anyString())).thenReturn(weatherInfo);

        // When & Then
        mockMvc.perform(get("/api/weather/chatclient/{city}", city))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.city").value(city))
            .andExpect(jsonPath("$.currentConditions").value("Partly cloudy"))
            .andExpect(jsonPath("$.temperature").value(19.5))
            .andExpect(jsonPath("$.humidity").value(70))
            .andExpect(jsonPath("$.forecast").value("Scattered clouds throughout the day"));
    }

    @Test
    void getWeather_withSpecialCharactersInCity_shouldHandleCorrectly() throws Exception {
        // Given
        String city = "São Paulo";
        WeatherService.WeatherInfo weatherInfo = new WeatherService.WeatherInfo(
            city,
            "Rainy",
            24.0,
            85,
            "Heavy rain expected"
        );
        
        when(weatherService.getWeatherWithChatClient("São Paulo")).thenReturn(weatherInfo);

        // When & Then
        mockMvc.perform(get("/api/weather/chatclient/{city}", city))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.city").value(city));
    }

    @Test
    void getWeather_withEmptyCity_shouldReturn404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weather/chatclient/"))
            .andExpect(status().isNotFound());
    }
}