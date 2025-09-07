package com.abcm0018.inventarioautomatizado.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product REST API")
                        .description("Some custom description of API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ana Belén")
                                .email("abcm0018@red.ujaen.es"))
                        .license(new License().name("License of API").url("API License URL")));
    }

}
