package com.example.service;

import com.example.dto.ExternalTodoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ExternalTodoClientIntegrationTest {

    private ServerSocket serverSocket;
    private Thread serverThread;
    private CloseableHttpClient httpClient;
    private ExternalTodoClient client;

    @BeforeEach
    public void setUp() throws Exception {
        serverSocket = new ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"));
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = serverSocket.accept()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    do {
                        line = reader.readLine();
                    } while (line != null && !line.isEmpty());

                    byte[] body = "{\"userId\":7,\"id\":42,\"title\":\"Integration response\",\"completed\":false}"
                            .getBytes(StandardCharsets.UTF_8);
                    OutputStream output = socket.getOutputStream();
                    String headers = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: "
                            + body.length + "\r\nConnection: close\r\n\r\n";
                    output.write(headers.getBytes(StandardCharsets.UTF_8));
                    output.write(body);
                    output.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        httpClient = HttpClients.createDefault();
        client = new ExternalTodoClient(httpClient, new ObjectMapper(), "http://127.0.0.1:" + serverSocket.getLocalPort());
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    @Test
    public void successfulRequestParsesJsonIntoDto() throws Exception {
        ExternalTodoDTO todo = client.fetchTodo(42L);

        assertEquals(Long.valueOf(7L), todo.getUserId());
        assertEquals(Long.valueOf(42L), todo.getId());
        assertEquals("Integration response", todo.getTitle());
        assertFalse(todo.getCompleted());
    }
}