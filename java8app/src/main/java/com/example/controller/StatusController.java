package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Arrays;

/**
 * Status Controller - serves a simple home page with navigation to different features.
 * Each feature (Users, Documents, etc.) has its own dedicated page for clarity.
 * Requires authentication - redirects to /login if not authenticated.
 */
@RestController
public class StatusController {

    @Autowired
    private Environment environment;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(HttpSession session) {
        // Check if user is authenticated
        String authenticatedUser = (String) session.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            // Redirect to login page
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head><meta http-equiv='refresh' content='0; URL=/login'/></head>\n" +
                    "<body>Redirecting to login...</body>\n" +
                    "</html>";
        }
        String authenticatedUserFullName = (String) session.getAttribute("authenticatedUserFullName");
        if (authenticatedUserFullName == null) {
            authenticatedUserFullName = authenticatedUser;
        }
        String[] profiles = environment.getActiveProfiles();
        String activeProfile = profiles.length > 0 ? profiles[0] : "default";
        boolean isProd = profiles.length > 0 && profiles[0].equals("prod");

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Document Management System</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body {\n" +
                "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\n" +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "            min-height: 100vh;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .container { max-width: 900px; width: 100%; }\n" +
                "        header {\n" +
                "            text-align: center;\n" +
                "            color: white;\n" +
                "            margin-bottom: 50px;\n" +
                "        }\n" +
                "        header h1 {\n" +
                "            font-size: 42px;\n" +
                "            margin-bottom: 10px;\n" +
                "            text-shadow: 0 2px 10px rgba(0,0,0,0.2);\n" +
                "        }\n" +
                "        header p {\n" +
                "            font-size: 18px;\n" +
                "            opacity: 0.95;\n" +
                "        }\n" +
                "        .user-info {\n" +
                "            background: rgba(255,255,255,0.15);\n" +
                "            padding: 8px 16px;\n" +
                "            border-radius: 6px;\n" +
                "            display: inline-block;\n" +
                "            color: white;\n" +
                "            margin-bottom: 20px;\n" +
                "            font-size: 13px;\n" +
                "        }\n" +
                "        .status {\n" +
                "            background: rgba(255,255,255,0.15);\n" +
                "            padding: 12px 24px;\n" +
                "            border-radius: 20px;\n" +
                "            display: inline-block;\n" +
                "            margin-bottom: 20px;\n" +
                "            margin-left: 10px;\n" +
                "            backdrop-filter: blur(10px);\n" +
                "            border: 1px solid rgba(255,255,255,0.2);\n" +
                "        }\n" +
                "        .nav-grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
                "            gap: 20px;\n" +
                "        }\n" +
                "        .nav-card {\n" +
                "            background: white;\n" +
                "            border-radius: 15px;\n" +
                "            padding: 30px 25px;\n" +
                "            box-shadow: 0 10px 30px rgba(0,0,0,0.2);\n" +
                "            text-decoration: none;\n" +
                "            color: #333;\n" +
                "            transition: all 0.3s ease;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .nav-card:hover {\n" +
                "            transform: translateY(-8px);\n" +
                "            box-shadow: 0 20px 40px rgba(0,0,0,0.3);\n" +
                "        }\n" +
                "        .nav-card.disabled {\n" +
                "            opacity: 0.5;\n" +
                "            pointer-events: none;\n" +
                "            background: #f0f0f0;\n" +
                "        }\n" +
                "        .nav-card .icon {\n" +
                "            font-size: 40px;\n" +
                "            margin-bottom: 12px;\n" +
                "        }\n" +
                "        .nav-card h3 {\n" +
                "            font-size: 18px;\n" +
                "            margin-bottom: 8px;\n" +
                "            color: #667eea;\n" +
                "        }\n" +
                "        .nav-card p {\n" +
                "            font-size: 13px;\n" +
                "            color: #999;\n" +
                "            line-height: 1.5;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            color: white;\n" +
                "            margin-top: 60px;\n" +
                "            font-size: 13px;\n" +
                "            opacity: 0.8;\n" +
                "        }\n" +
                "        .footer a {\n" +
                "            color: white;\n" +
                "            text-decoration: underline;\n" +
                "        }\n" +
                "        .logout-btn {\n" +
                "            position: absolute;\n" +
                "            top: 20px;\n" +
                "            right: 20px;\n" +
                "            background: rgba(255,255,255,0.2);\n" +
                "            color: white;\n" +
                "            border: 1px solid rgba(255,255,255,0.3);\n" +
                "            padding: 8px 16px;\n" +
                "            border-radius: 6px;\n" +
                "            cursor: pointer;\n" +
                "            font-size: 13px;\n" +
                "            font-weight: 600;\n" +
                "            transition: all 0.3s;\n" +
                "        }\n" +
                "        .logout-btn:hover {\n" +
                "            background: rgba(255,255,255,0.3);\n" +
                "            border-color: rgba(255,255,255,0.5);\n" +
                "        }\n" +
                "        @media (max-width: 600px) {\n" +
                "            header h1 { font-size: 28px; }\n" +
                "            .nav-grid { grid-template-columns: 1fr; }\n" +
                "            .logout-btn { position: static; margin-bottom: 20px; }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <form method=\"POST\" action=\"/logout\" style=\"position: absolute; top: 20px; right: 20px;\">\n" +
                "        <button type=\"submit\" class=\"logout-btn\">🚪 Logout</button>\n" +
                "    </form>\n" +
                "    <div class=\"container\">\n" +
                "        <header>\n" +
                "            <div class=\"user-info\">👤 " + authenticatedUserFullName + "</div>\n" +
                "            <div class=\"status\">✓ " + activeProfile.toUpperCase() + " PROFILE</div>\n" +
                "            <h1>Document Management</h1>\n" +
                "            <p>Simple CRUD Operations for All Entities</p>\n" +
                "        </header>\n" +
                "\n" +
                "        <div class=\"nav-grid\">\n" +
                (isProd ? 
                "            <a href=\"/pages/users.html\" class=\"nav-card\">\n" +
                "                <div class=\"icon\">👥</div>\n" +
                "                <h3>Users</h3>\n" +
                "                <p>Manage users, create, update, delete</p>\n" +
                "            </a>\n" +
                "            <a href=\"/pages/documents.html\" class=\"nav-card\">\n" +
                "                <div class=\"icon\">📄</div>\n" +
                "                <h3>Documents</h3>\n" +
                "                <p>CRUD operations for documents</p>\n" +
                "            </a>\n" +
                "            <a href=\"/pages/sharing.html\" class=\"nav-card\">\n" +
                "                <div class=\"icon\">🔗</div>\n" +
                "                <h3>Sharing</h3>\n" +
                "                <p>Share and manage document access</p>\n" +
                "            </a>\n" +
                "            <a href=\"/pages/categories.html\" class=\"nav-card\">\n" +
                "                <div class=\"icon\">🏷️</div>\n" +
                "                <h3>Categories</h3>\n" +
                "                <p>Create and manage categories</p>\n" +
                "            </a>\n" +
                "            <a href=\"/pages/audit.html\" class=\"nav-card\">\n" +
                "                <div class=\"icon\">📋</div>\n" +
                "                <h3>Audit Logs</h3>\n" +
                "                <p>View access logs and statistics</p>\n" +
                "            </a>\n" :
                "            <div class=\"nav-card disabled\">\n" +
                "                <div class=\"icon\">🔒</div>\n" +
                "                <h3>Database Features Disabled</h3>\n" +
                "                <p>Start with --spring.profiles.active=prod to enable all features</p>\n" +
                "            </div>\n") +
                "            <a href=\"/swagger-ui.html\" class=\"nav-card\">\n" +
                "                <div class=\"icon\">📚</div>\n" +
                "                <h3>API Docs</h3>\n" +
                "                <p>Swagger UI - Interactive API docs</p>\n" +
                "            </a>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Java 8 Spring Application • <a href=\"/v3/api-docs\" target=\"_blank\">OpenAPI Spec</a></p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
