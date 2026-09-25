package com.example.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * OpenAPI / Swagger UI Configuration
 * For springdoc-openapi-starter-webmvc-ui 2.x with Spring Framework 6.x
 * 
 * Defines OpenAPI beans manually for use with manual Jetty + Spring Framework setup.
 */
@Configuration
/*TODO
@Import({
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SwaggerUiConfigProperties.class,
        SwaggerUiConfigParameters.class,
        SwaggerUiOAuthProperties.class,
        SpringDocWebMvcConfiguration.class,
        MultipleOpenApiSupportConfiguration.class,
        SwaggerConfig.class,
        SpringDocUIConfiguration.class
})
*/
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Management API")
                .version("3.0.0")
                .description("REST API for user management with Spring Framework 6.x and Java 21")
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("users")
            .pathsToMatch("/api/users/**")
            .build();
    }
}
