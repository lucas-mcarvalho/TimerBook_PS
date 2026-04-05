package com.timerbook.TimerBook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPi {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TimerBook API")
                        .description("Documentação da API do TimerBook")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("TimerBook")
                                .email("seu@email.com"))
                        .license(new License()
                                .name("Apache 2.0")));
    }
}
