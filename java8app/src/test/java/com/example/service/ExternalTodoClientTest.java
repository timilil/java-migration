package com.example.service;

import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExternalTodoClientTest {

    @Test(expected = IOException.class)
    public void rejectsUnsuccessfulHttpResponses() throws Exception {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 503, "Unavailable"));
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        new ExternalTodoClient(httpClient, new com.fasterxml.jackson.databind.ObjectMapper(), "http://localhost")
                .fetchTodo(1L);
    }
}