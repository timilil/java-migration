package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Web MVC Configuration - enables Spring MVC defaults (JSON message converters,
 * content negotiation, etc.) since we are not using Spring Boot auto-configuration.
 * Also registers static resource handlers for the bundled dashboard pages.
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pages/**")
                .addResourceLocations("classpath:/static/pages/");
    }
}
