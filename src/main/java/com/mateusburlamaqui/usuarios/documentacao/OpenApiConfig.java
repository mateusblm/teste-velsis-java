package com.mateusburlamaqui.usuarios.documentacao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String SEGURANCA_BASIC = "basicAuth";

    @Bean
    public OpenAPI usuariosOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Usuários API")
                .description("API REST para cadastro, consulta e atualização de usuários.")
                .version("1.0.0"))
            .components(new Components()
                .addSecuritySchemes(SEGURANCA_BASIC, new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}
