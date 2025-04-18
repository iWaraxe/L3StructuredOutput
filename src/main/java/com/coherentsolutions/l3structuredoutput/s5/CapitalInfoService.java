package com.coherentsolutions.l3structuredoutput.s5;

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
                      
                      For each country, you MUST provide ALL of the following information about its capital city:
                       - The exact name of the capital city (as city field)
                       - The population in millions as a number (as population field)
                       - The region or state where the capital is located (as region field)
                       - The primary language spoken in the capital (as language field)
                       - The currency used (as currency field)
                       - An array of at least 2 famous landmarks (as landmarks field)
                      
                      Do not omit any fields. If you are unsure about the exact value, provide your best estimate.
                      
                      Format the response as a map where the country name is the key and the capital information is the value.
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