package com.example.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Status Controller - serves the interactive dashboard from the bundled
 * static/index.html resource at the root path, but only for authenticated
 * sessions. Unauthenticated requests are redirected to /login.
 */
@RestController
public class StatusController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String status(HttpSession session) throws IOException {
        String authenticatedUser = (String) session.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            return "<!DOCTYPE html>\n<html>\n<head>" +
                    "<meta http-equiv=\"refresh\" content=\"0; URL=/login\"/>" +
                    "</head></html>";
        }

        try (InputStream in = new ClassPathResource("static/index.html").getInputStream()) {
            byte[] bytes = readAll(in);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int read;
        while ((read = in.read(data)) != -1) {
            buffer.write(data, 0, read);
        }
        return buffer.toByteArray();
    }
}
