package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Status Controller - serves a simple HTML status page at the root path
 * showing the application is running and which profile is active.
 * Available in all profiles (not restricted).
 */
@RestController
public class StatusController {

    @Autowired
    private Environment environment;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String status() {
        String[] profiles = environment.getActiveProfiles();
        String activeProfile = profiles.length > 0
                ? Arrays.toString(profiles)
                : "default";

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Java 8 Spring App - Status</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background: #f4f6f8; color: #2c3e50; " +
                "               display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }\n" +
                "        .card { background: #fff; padding: 2.5rem 3rem; border-radius: 8px; " +
                "                box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }\n" +
                "        .status-badge { display: inline-block; background: #2ecc71; color: #fff; " +
                "                        padding: 0.3rem 0.8rem; border-radius: 4px; font-weight: bold; margin-bottom: 1rem; }\n" +
                "        h1 { margin: 0 0 0.5rem 0; font-size: 1.5rem; }\n" +
                "        .profile { font-size: 1.1rem; margin-top: 0.5rem; }\n" +
                "        .profile span { font-weight: bold; color: #2980b9; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"card\">\n" +
                "        <div class=\"status-badge\">&#9679; RUNNING</div>\n" +
                "        <h1>Java 8 Spring Application</h1>\n" +
                "        <p>The application is up and running.</p>\n" +
                "        <p class=\"profile\">Active profile: <span>" + activeProfile + "</span></p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
