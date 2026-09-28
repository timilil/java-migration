package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Application Configuration - Spring beans and configuration
 */
@Configuration
public class AppConfig {
    
    /**
     * Create a RestTemplate bean for HTTP calls
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * Create an HttpClient bean for custom HTTP operations
     * @return HttpClient instance
     */
    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(10)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
