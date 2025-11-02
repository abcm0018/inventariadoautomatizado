package com.abcm0018.sai.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringTemplateEngine springTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();

        // Resolver que interpreta strings como plantillas
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML"); // el modo HTML
        resolver.setCacheable(false); // sin cacheo (para desarrollo)

        engine.setTemplateResolver(resolver);
        return engine;
    }
}
