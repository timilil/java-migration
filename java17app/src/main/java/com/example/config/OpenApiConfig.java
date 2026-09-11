package com.example.config;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.core.SpringDocUIConfiguration;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.MultipleOpenApiSupportConfiguration;
import org.springdoc.webmvc.core.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Manually wires up SpringDoc OpenAPI / Swagger UI beans.
 * <p>
 * springdoc-openapi-ui normally relies on Spring Boot's auto-configuration
 * mechanism (spring.factories / AutoConfiguration.imports) to register these
 * beans automatically. Since this application does not use Spring Boot
 * (manual Jetty + Spring bootstrap), we import the same configuration
 * classes directly. springdoc-openapi-ui already brings spring-boot and
 * spring-boot-autoconfigure onto the classpath transitively, so the
 * {@code @ConditionalOnBean}/{@code @ConditionalOnProperty} annotations
 * on these classes work correctly here as well.
 * <p>
 * NOTE: We intentionally do NOT use {@code @EnableConfigurationProperties}
 * here - its registrar calls a Spring Framework 5.3+ only
 * BeanDefinitionBuilder overload, which is incompatible with the Spring
 * Framework 5.2.x version pinned in this project. The
 * {@code @ConfigurationProperties} fields on these classes simply keep
 * their default values (which already match this project's
 * application*.properties: swagger-ui path "/swagger-ui.html",
 * api-docs path "/v3/api-docs", both enabled).
 * <p>
 * Import order matters: {@link SpringDocConfiguration} and
 * {@link SwaggerUiConfigProperties} must come before the classes that are
 * conditional on their presence.
 */
@Configuration
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
public class OpenApiConfig {
}
