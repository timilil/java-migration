package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration - enables Spring MVC defaults (JSON message converters,
 * content negotiation, etc.) and configures static resource mapping for Swagger UI.
 * Manual resource configuration needed since we are not using Spring Boot auto-configuration.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configure static resource mappings for Swagger UI.
     * Maps /swagger-ui.html and /swagger-ui/* to classpath resources provided by springdoc-openapi.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map swagger-ui.html and related resources
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/swagger-ui/");
        
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        registry.addResourceHandler("/v3/api-docs/**")
            .addResourceLocations("classpath:/META-INF/resources/");
    }
}
