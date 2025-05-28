package com.coherentsolutions.l3structuredoutput.s7;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating when to use ChatClient vs ChatModel approaches
 */
@RestController
@RequestMapping("/finance")
public class FinancialAdviceController {

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final FinancialAdviceService financialAdviceService;

    @Autowired
    public FinancialAdviceController(
            ChatClient.Builder chatClientBuilder,
            ChatModel chatModel,
            FinancialAdviceService financialAdviceService) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
        this.financialAdviceService = financialAdviceService;
    }

    /**
     * Simple endpoint using ChatClient - good for straightforward queries
     */
    @GetMapping("/budget-tips")
    public List<String> getBudgetTips(@RequestParam(defaultValue = "2000") double monthlyIncome) {
        // Using ChatClient with ListOutputConverter is ideal for simple list responses
        return chatClient.prompt()
                .user(u -> u.text("Give me 5 budget tips for someone making ${income} per month")
                        .param("income", monthlyIncome))
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
    }

    /**
     * Complex endpoint using ChatModel with model-specific options - good for advanced configurations
     */
    @PostMapping("/investment-analysis")
    public FinancialAdviceService.InvestmentOption analyzeInvestment(@RequestBody Map<String, Object> investmentDetails) {
        // Using ChatModel when you need more control over model options
        BeanOutputConverter<FinancialAdviceService.InvestmentOption> converter =
                new BeanOutputConverter<>(FinancialAdviceService.InvestmentOption.class);

        String format = converter.getFormat();
        String promptTemplate = """
                Analyze this investment opportunity and provide a detailed assessment:
                
                Investment Name: {name}
                Type: {type}
                Current Price: ${price}
                Historical Performance: {history}
                
                Provide a thorough analysis with name, type, risk level (1-10), 
                expected return percentage, recommended time horizon, 
                at least 3 pros and 3 cons.
                {format}
                """;

        // Create model-specific options using portable ChatOptions
        ChatOptions options = ChatOptions.builder()
                .temperature(0.2) // Lower temperature for more factual/analytical responses
                .maxTokens(1500)   // Higher token limit for detailed analysis
                .build();

        Prompt prompt = new PromptTemplate(promptTemplate)
                .create(Map.of("name", investmentDetails.get("name"),
                        "type", investmentDetails.get("type"),
                        "price", investmentDetails.get("price"),
                        "history", investmentDetails.get("history"),
                        "format", format));

        String response = chatModel.call(
                        new Prompt(prompt.getInstructions(), options))
                .getResult()
                .getOutput()
                .getText();

        return converter.convert(response);
    }

    /**
     * Endpoint using ChatClient for complete financial plan
     */
    @GetMapping("/plan")
    public FinancialAdviceService.FinancialPlan getFinancialPlan(
            @RequestParam String goal,
            @RequestParam double budget,
            @RequestParam String riskTolerance,
            @RequestParam int years) {

        return financialAdviceService.generateFinancialPlanWithChatClient(
                goal, budget, riskTolerance, years);
    }

    /**
     * Endpoint using ChatModel for investment options
     */
    @GetMapping("/investment-options")
    public List<FinancialAdviceService.InvestmentOption> getInvestmentOptions(
            @RequestParam String type,
            @RequestParam String risk,
            @RequestParam double minReturn) {

        // Using ChatModel approach with ParameterizedTypeReference
        BeanOutputConverter<List<FinancialAdviceService.InvestmentOption>> converter =
                new BeanOutputConverter<>(
                        new ParameterizedTypeReference<List<FinancialAdviceService.InvestmentOption>>() {});

        String format = converter.getFormat();
        String promptTemplate = """
                Suggest a list of 5 {type} investment options with risk level {risk} 
                and minimum expected return of {return}%.
                
                For each option, provide the name, exact type, risk level (1-10),
                expected return percentage, recommended time horizon,
                and at least 3 pros and 3 cons.
                {format}
                """;

        Prompt prompt = new PromptTemplate(promptTemplate)
                .create(Map.of("type", type,
                        "risk", risk,
                        "return", minReturn,
                        "format", format));

        String response = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        return converter.convert(response);
    }
}