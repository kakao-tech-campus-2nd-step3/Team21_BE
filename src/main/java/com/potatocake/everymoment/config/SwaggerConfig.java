package com.potatocake.everymoment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("API").version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                );
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(addSecurityItemToAllEndpointsExceptLogin())
                .build();
    }

    private OpenApiCustomizer addSecurityItemToAllEndpointsExceptLogin() {
        return openApi -> {
            SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
            openApi.getPaths().forEach((path, item) -> {
                if (!"/api/members/login".equals(path) && !"/api/members/anonymous-login".equals(path)) {
                    item.readOperations().forEach(operation -> {
                        operation.addSecurityItem(securityRequirement);
                    });
                }
            });
        };
    }

}
