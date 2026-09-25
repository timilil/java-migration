package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.HttpClients;

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
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(10)
                .build();
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
