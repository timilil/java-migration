package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

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
    public HttpClient httpClient() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);
        return HttpClients.custom()
                .setConnectionManager(connManager)
                .build();
    }
}
