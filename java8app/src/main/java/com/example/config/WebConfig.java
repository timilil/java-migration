package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Web MVC Configuration - enables Spring MVC defaults (JSON message converters,
 * content negotiation, etc.) since we are not using Spring Boot auto-configuration.
 */
@Configuration
@EnableWebMvc
public class WebConfig {
}
