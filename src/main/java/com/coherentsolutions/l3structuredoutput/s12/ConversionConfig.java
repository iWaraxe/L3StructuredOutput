package com.coherentsolutions.l3structuredoutput.s12;

import com.coherentsolutions.l3structuredoutput.s12.converters.CustomDateConverter;
import com.coherentsolutions.l3structuredoutput.s12.converters.DurationConverter;
import com.coherentsolutions.l3structuredoutput.s12.converters.MoneyConverter;
import com.coherentsolutions.l3structuredoutput.s12.formatters.CustomDateFormatter;
import com.coherentsolutions.l3structuredoutput.s12.formatters.MoneyFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for custom converters and formatters.
 */
@Configuration
public class ConversionConfig implements WebMvcConfigurer {
    
    private final MoneyConverter moneyConverter;
    private final CustomDateConverter customDateConverter;
    private final DurationConverter durationConverter;
    private final MoneyFormatter moneyFormatter;
    private final CustomDateFormatter customDateFormatter;
    
    public ConversionConfig(MoneyConverter moneyConverter,
                          CustomDateConverter customDateConverter,
                          DurationConverter durationConverter,
                          MoneyFormatter moneyFormatter,
                          CustomDateFormatter customDateFormatter) {
        this.moneyConverter = moneyConverter;
        this.customDateConverter = customDateConverter;
        this.durationConverter = durationConverter;
        this.moneyFormatter = moneyFormatter;
        this.customDateFormatter = customDateFormatter;
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Add converters
        registry.addConverter(moneyConverter);
        registry.addConverter(customDateConverter);
        registry.addConverter(durationConverter);
        
        // Add formatters
        registry.addFormatter(moneyFormatter);
        registry.addFormatter(customDateFormatter);
    }
    
    @Bean
    public FormattingConversionService customConversionService() {
        FormattingConversionService service = new FormattingConversionService();
        
        // Register converters
        service.addConverter(moneyConverter);
        service.addConverter(customDateConverter);
        service.addConverter(durationConverter);
        
        // Register formatters
        service.addFormatter(moneyFormatter);
        service.addFormatter(customDateFormatter);
        
        return service;
    }
}