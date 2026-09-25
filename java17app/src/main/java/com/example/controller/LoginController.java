package com.example.controller;

import com.example.entity.AuthUser;
import com.example.entity.User;
import com.example.repository.AuthUserRepository;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * LoginController - handles user authentication and session management
 */
@Controller
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @Autowired
    private AuthUserRepository authUserRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Show login page - GET /login
     */
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String loginPage() {
        logger.info("GET /login - Displaying login form");
        return getLoginHTML();
    }
    
    /**
     * Handle login form submission - POST /login
     */
    @PostMapping(value = "/login")
    public RedirectView handleLogin(@RequestParam String username, 
                                      @RequestParam String password,
                                      HttpSession session) {
        logger.info("POST /login - Attempting login for user: {}", username);
        logger.debug("Password provided: {} chars", password.length());
        
        // First, try to authenticate against auth_user table (for dedicated auth users like admin)
        Optional<AuthUser> authUser = authUserRepository.findByUsername(username);
        logger.debug("AuthUser lookup result for {}: {}", username, authUser.isPresent() ? "FOUND" : "NOT FOUND");
        
        if (authUser.isPresent()) {
            AuthUser user = authUser.get();
            logger.debug("AuthUser found: id={}, username={}, fullName={}", user.getId(), user.getUsername(), user.getFullName());
            logger.debug("Comparing passwords: provided='{}' vs stored='{}'", password, user.getPassword());
            
            if (user.getPassword().equals(password)) {
                // Authentication successful from auth_user table
                logger.info("Login successful for auth user: {}", username);
                
                // Set session attributes
                session.setAttribute("authenticatedUser", username);
                session.setAttribute("authenticatedUserId", user.getId());
                session.setAttribute("authenticatedUserFullName", user.getFullName());
                
                // Redirect to home page
                return new RedirectView("/", true);
            } else {
                logger.warn("Password mismatch for auth user: {}", username);
            }
        }
        
        // If not found in auth_user, try users table (for users created via user management)
        Optional<User> regularUser = userRepository.findByUsername(username);
        logger.debug("User lookup result for {}: {}", username, regularUser.isPresent() ? "FOUND" : "NOT FOUND");
        
        if (regularUser.isPresent()) {
            User user = regularUser.get();
            logger.debug("User found: id={}, username={}, fullName={}", user.getId(), user.getUsername(), user.getFullName());
            logger.debug("Comparing passwords: provided='{}' vs stored='{}'", password, user.getPassword());
            
            if (user.getPassword().equals(password)) {
                // Authentication successful from users table
                logger.info("Login successful for regular user: {}", username);
                
                // Set session attributes
                session.setAttribute("authenticatedUser", username);
                session.setAttribute("authenticatedUserId", user.getId());
                session.setAttribute("authenticatedUserFullName", user.getFullName());
                
                // Redirect to home page
                return new RedirectView("/", true);
            } else {
                logger.warn("Password mismatch for regular user: {}", username);
            }
        }
        
        // Authentication failed - user not found or password mismatch
        logger.warn("Login failed for user: {} - invalid credentials or user not found", username);
        return new RedirectView("/login", true);
    }
    
    /**
     * Handle logout - POST /logout
     */
    @PostMapping(value = "/logout")
    public RedirectView handleLogout(HttpSession session) {
        logger.info("POST /logout - User logging out");
        
        // Get username for logging
        String username = (String) session.getAttribute("authenticatedUser");
        if (username != null) {
            logger.info("User {} logged out", username);
        }
        
        // Invalidate session
        session.invalidate();
        
        // Redirect to login page
        return new RedirectView("/login", true);
    }
    
    /**
     * Return login page HTML
     */
    private String getLoginHTML() {
        return "<!DOCTYPE html>\n" +
"<html lang=\"en\">\n" +
"<head>\n" +
"    <meta charset=\"UTF-8\">\n" +
"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
"    <title>Login - Document Management System</title>\n" +
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
"        .container { max-width: 400px; width: 100%; }\n" +
"        .login-box {\n" +
"            background: white;\n" +
"            border-radius: 15px;\n" +
"            padding: 40px;\n" +
"            box-shadow: 0 10px 40px rgba(0,0,0,0.3);\n" +
"        }\n" +
"        h1 {\n" +
"            text-align: center;\n" +
"            color: #333;\n" +
"            margin-bottom: 10px;\n" +
"            font-size: 28px;\n" +
"        }\n" +
"        .subtitle {\n" +
"            text-align: center;\n" +
"            color: #999;\n" +
"            margin-bottom: 30px;\n" +
"            font-size: 13px;\n" +
"        }\n" +
"        .form-group {\n" +
"            margin-bottom: 20px;\n" +
"        }\n" +
"        label {\n" +
"            display: block;\n" +
"            margin-bottom: 8px;\n" +
"            color: #333;\n" +
"            font-weight: 600;\n" +
"            font-size: 14px;\n" +
"        }\n" +
"        input[type=\"text\"],\n" +
"        input[type=\"password\"] {\n" +
"            width: 100%;\n" +
"            padding: 12px 15px;\n" +
"            border: 2px solid #e0e0e0;\n" +
"            border-radius: 8px;\n" +
"            font-size: 14px;\n" +
"            transition: border-color 0.3s;\n" +
"            font-family: inherit;\n" +
"        }\n" +
"        input[type=\"text\"]:focus,\n" +
"        input[type=\"password\"]:focus {\n" +
"            outline: none;\n" +
"            border-color: #667eea;\n" +
"        }\n" +
"        button {\n" +
"            width: 100%;\n" +
"            padding: 12px;\n" +
"            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
"            color: white;\n" +
"            border: none;\n" +
"            border-radius: 8px;\n" +
"            font-size: 16px;\n" +
"            font-weight: 600;\n" +
"            cursor: pointer;\n" +
"            transition: transform 0.2s;\n" +
"        }\n" +
"        button:hover {\n" +
"            transform: translateY(-2px);\n" +
"        }\n" +
"        .demo-info {\n" +
"            background: #e7f3ff;\n" +
"            color: #004085;\n" +
"            padding: 12px;\n" +
"            border-radius: 8px;\n" +
"            margin-bottom: 20px;\n" +
"            font-size: 13px;\n" +
"            line-height: 1.6;\n" +
"            border: 1px solid #b8daff;\n" +
"        }\n" +
"        .demo-info strong { display: block; margin-bottom: 5px; }\n" +
"    </style>\n" +
"</head>\n" +
"<body>\n" +
"    <div class=\"container\">\n" +
"        <div class=\"login-box\">\n" +
"            <h1>🔐 Login</h1>\n" +
"            <p class=\"subtitle\">Document Management System</p>\n" +
"            <div class=\"demo-info\">\n" +
"                <strong>Demo Credentials:</strong>\n" +
"                Username: admin<br>\n" +
"                Password: admin123\n" +
"            </div>\n" +
"            <form method=\"POST\" action=\"/login\">\n" +
"                <div class=\"form-group\">\n" +
"                    <label for=\"username\">Username</label>\n" +
"                    <input type=\"text\" id=\"username\" name=\"username\" required autofocus>\n" +
"                </div>\n" +
"                <div class=\"form-group\">\n" +
"                    <label for=\"password\">Password</label>\n" +
"                    <input type=\"password\" id=\"password\" name=\"password\" required>\n" +
"                </div>\n" +
"                <button type=\"submit\">Sign In</button>\n" +
"            </form>\n" +
"        </div>\n" +
"    </div>\n" +
"</body>\n" +
"</html>";
    }
}
