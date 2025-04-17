package com.coherentsolutions.l3structuredoutput.services.s5;

import com.coherentsolutions.l3structuredoutput.model.s5.CapitalInfo;
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
public class CapitalInfoService {

    private final OpenAiChatModel chatModel;

    public CapitalInfoService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Gets detailed information about a capital city.
     */
    public CapitalInfo getCapitalInfo(String country) {
        BeanOutputConverter<CapitalInfo> converter =
                new BeanOutputConverter<>(CapitalInfo.class);

        String format = converter.getFormat();

        String promptText = """
            Provide detailed information about the capital city of {country}.
            Include the city name, population (in millions), region/state,
            primary language, currency, and an array of at least 3 famous landmarks.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "country", country,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }

    /**
     * Compares multiple capital cities.
     */
    public Map<String, CapitalInfo> compareCapitals(List<String> countries) {
        BeanOutputConverter<Map<String, CapitalInfo>> converter =
                new BeanOutputConverter<>(new ParameterizedTypeReference<Map<String, CapitalInfo>>() {});

        String format = converter.getFormat();

        String countriesList = String.join(", ", countries);

        String promptText = """
            Compare the capital cities of these countries: {countries}.
            
            For each country, provide detailed information about its capital city,
            including the city name, population (in millions), region/state,
            primary language, currency, and an array of at least 2 famous landmarks.
            
            Format the response as a map where the country name is the key and
            the capital information is the value.
            {format}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        String renderedPrompt = template.render(Map.of(
                "countries", countriesList,
                "format", format
        ));

        Prompt prompt = new Prompt(renderedPrompt);
        ChatResponse response = chatModel.call(prompt);

        String responseText = response.getResult().getOutput().getText();
        return converter.convert(responseText);
    }
}