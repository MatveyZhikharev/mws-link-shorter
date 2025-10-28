package ru.mws.link_shorter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI linkShorterOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Link Shorter API")
            .description("REST API сервис для сокращения длинных ссылок")
            .version("1.0.0")
            .contact(new Contact()
                .name("Link Shorter Team")
                .email("support@linkshorter.ru")));
  }
}