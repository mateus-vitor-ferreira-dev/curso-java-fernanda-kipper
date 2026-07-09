package com.eventostec.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger / OpenAPI (springdoc).
 *
 * <p>Define os metadados da documentação da API. Com a aplicação no ar:
 * <ul>
 *   <li>Swagger UI (interface): {@code /swagger-ui/index.html}</li>
 *   <li>Especificação OpenAPI (JSON): {@code /v3/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Metadados exibidos no topo da documentação (título, descrição, versão).
     *
     * @return a definição OpenAPI da EventosTec API
     */
    @Bean
    public OpenAPI eventostecOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventosTec API")
                        .description("API REST para gerenciamento de eventos de tecnologia "
                                + "(presenciais e remotos), com cupons de desconto, filtros e "
                                + "listagem paginada de eventos futuros.")
                        .version("1.0.0")
                        .contact(new Contact().name("Mateus Vitor Ferreira"))
                        .license(new License().name("Projeto do curso da Fernanda Kipper")));
    }
}
