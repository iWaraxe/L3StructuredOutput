package com.coherentsolutions.l3structuredoutput.s7;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service demonstrating complex structured output conversion for financial advice
 */
@Service
public class FinancialAdviceService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    @Autowired
    public FinancialAdviceService(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
    }

    /**
     * Record representing a financial investment opportunity
     */
    public record InvestmentOption(
            String name,
            String type,
            Integer riskLevel,
            Double expectedReturn,
            String timeHorizon,
            List<String> pros,
            List<String> cons
    ) {}

    /**
     * Record representing a personalized financial plan
     */
    public record FinancialPlan(
            String goalSummary,
            List<InvestmentOption> recommendedInvestments,
            Double monthlyContribution,
            Integer timeFrameYears,
            String riskProfile,
            List<String> additionalAdvice
    ) {}

    /**
     * High-level ChatClient approach for generating a financial plan
     */
    public FinancialPlan generateFinancialPlanWithChatClient(
            String financialGoal, double monthlyBudget, String riskTolerance, int timeHorizon) {

        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("Create a personalized financial plan for someone with the following parameters:\n" +
                                "- Financial Goal: {goal}\n" +
                                "- Monthly Budget: ${budget}\n" +
                                "- Risk Tolerance: {risk}\n" +
                                "- Time Horizon: {years} years\n\n" +
                                "Include a goal summary, 3-4 recommended investment options with details " +
                                "(name, type, risk level 1-10, expected return rate, time horizon, pros, and cons), " +
                                "suggested monthly contribution, timeframe in years, risk profile description, " +
                                "and additional financial advice.")
                        .param("goal", financialGoal)
                        .param("budget", monthlyBudget)
                        .param("risk", riskTolerance)
                        .param("years", timeHorizon))
                .call()
                .entity(FinancialPlan.class);
    }

    /**
     * Low-level ChatModel approach for generating a financial plan
     */
    public FinancialPlan generateFinancialPlanWithChatModel(
            String financialGoal, double monthlyBudget, String riskTolerance, int timeHorizon) {

        // Create the output converter
        BeanOutputConverter<FinancialPlan> outputConverter = new BeanOutputConverter<>(FinancialPlan.class);
        String format = outputConverter.getFormat();

        // Create and render the template
        String promptTemplate = """
                Create a personalized financial plan for someone with the following parameters:
                - Financial Goal: {goal}
                - Monthly Budget: ${budget}
                - Risk Tolerance: {risk}
                - Time Horizon: {years} years
                
                Include a goal summary, 3-4 recommended investment options with details 
                (name, type, risk level 1-10, expected return rate, time horizon, pros, and cons),
                suggested monthly contribution, timeframe in years, risk profile description,
                and additional financial advice.
                {format}
                """;

        Map<String, Object> parameters = Map.of(
                "goal", financialGoal,
                "budget", monthlyBudget,
                "risk", riskTolerance,
                "years", timeHorizon,
                "format", format
        );

        PromptTemplate template = new PromptTemplate(promptTemplate);
        String renderedPrompt = template.render(parameters);
        Prompt prompt = new Prompt(renderedPrompt);

        // Call the model
        String responseText = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        // Convert the response
        return outputConverter.convert(responseText);
    }

    /**
     * Alternative approach using generic ParameterizedTypeReference for a list of investment options
     */
    public List<InvestmentOption> findInvestmentOptionsWithChatClient(
            String investmentType, String riskLevel, double minReturn) {

        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("Suggest a list of {count} {type} investment options with " +
                                "risk level {risk} and minimum expected return of {return}%. " +
                                "For each option, provide the name, exact type, risk level (1-10), " +
                                "expected return percentage, recommended time horizon, " +
                                "and at least 3 pros and 3 cons.")
                        .param("count", 5)
                        .param("type", investmentType)
                        .param("risk", riskLevel)
                        .param("return", minReturn))
                .call()
                .entity(new ParameterizedTypeReference<List<InvestmentOption>>() {});
    }
}