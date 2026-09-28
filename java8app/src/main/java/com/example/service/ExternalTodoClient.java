package com.example.service;

import com.example.dto.ExternalTodoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ExternalTodoClient {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public ExternalTodoClient(CloseableHttpClient httpClient,
                              ObjectMapper objectMapper,
                              @Value("${external.api.base-url:https://jsonplaceholder.typicode.com}") String baseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public ExternalTodoDTO fetchTodo(long id) throws IOException {
        HttpGet request = new HttpGet(baseUrl + "/todos/" + id);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("External todo request failed with HTTP status " + statusCode);
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("External todo response had no body");
            }
            String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, ExternalTodoDTO.class);
        }
    }
}