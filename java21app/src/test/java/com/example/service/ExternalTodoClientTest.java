package com.example.service;

import com.example.dto.ExternalTodoDTO;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExternalTodoClientTest {

    @Test
    public void rejectsUnsuccessfulHttpResponses() throws Exception {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = CloseableHttpResponse.adapt(
            new BasicClassicHttpResponse(503, "Unavailable"));
        when(httpClient.execute(any(ClassicHttpRequest.class),
            org.mockito.ArgumentMatchers.<HttpClientResponseHandler<ExternalTodoDTO>>any())).thenAnswer(invocation -> {
                HttpClientResponseHandler<ExternalTodoDTO> responseHandler = invocation.getArgument(1);
                return responseHandler.handleResponse(response);
            });

        ExternalTodoClient client = new ExternalTodoClient(httpClient,
            new com.fasterxml.jackson.databind.ObjectMapper(), "http://localhost");
        assertThrows(IOException.class, () -> client.fetchTodo(1L));
    }
}