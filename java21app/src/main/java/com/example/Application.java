package com.example;

import com.example.config.SpringConfig;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Properties;

/**
 * Main Application entry point - Manual Jetty + Spring initialization (no Spring Boot)
 */
@ComponentScan(basePackages = "com.example")
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        try {
            // Determine active profile: command-line arg > system property > env var > default
            String activeProfile = parseProfileFromArgs(args);
            if (activeProfile == null) {
                activeProfile = System.getProperty("spring.profiles.active",
                                      System.getenv("SPRING_PROFILES_ACTIVE"));
            }
            if (activeProfile == null) {
                activeProfile = "dev"; // default profile
            }
            
            logger.info("Starting Java 8 Spring Application");
            logger.info("Active profile: {}", activeProfile);

            // Create Jetty server
            logger.info("Creating and starting Jetty server...");
            Server server = createAndStartJettyServer(activeProfile);
            
            logger.info("Server started successfully!");
            // Keep the application running
            server.join();
        } catch (Throwable e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parse "--spring.profiles.active=X" style argument from the command line.
     * Returns null if not present.
     */
    private static String parseProfileFromArgs(String[] args) {
        String prefix = "--spring.profiles.active=";
        for (String arg : args) {
            if (arg != null && arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return null;
    }

    /**
     * Create and start Jetty server with Spring context
     */
    private static Server createAndStartJettyServer(String activeProfile) throws Exception {
        // Create Spring web application context (not yet refreshed - see note below)
        logger.info("Creating Spring context with profile: {}", activeProfile);
        AnnotationConfigWebApplicationContext springContext = 
            createSpringContext(Application.class, activeProfile);
        logger.info("Spring context configured, will be refreshed by DispatcherServlet");

        // Create Jetty server
        int port = getServerPort(springContext);
        logger.info("Creating Jetty server on port: {}", port);
        Server server = new Server(port);

        // Create servlet context for Spring DispatcherServlet
        ServletContextHandler servletContext = new ServletContextHandler();
        servletContext.setContextPath("/");

        // Add static resource filter to serve Swagger UI and other static files
        servletContext.addFilter(StaticResourceFilter.class, "/*", null);

        // Add ClasspathResourceServlet to serve static resources from classpath
        ServletHolder resourceServlet = new ServletHolder("classpath-resources", ClasspathResourceServlet.class);
        servletContext.addServlet(resourceServlet, "/swagger-ui.html");
        servletContext.addServlet(resourceServlet, "/swagger-ui/*");
        servletContext.addServlet(resourceServlet, "/webjars/*");

        // Register DispatcherServlet with the Spring context.
        // IMPORTANT: The context must NOT be refreshed yet at this point. Beans
        // registered via @EnableWebMvc (e.g. resourceHandlerMapping) require a
        // ServletContext to be set on the WebApplicationContext before refresh.
        // DispatcherServlet.init() calls context.setServletContext(...) and then
        // refreshes the context itself, so it must receive an unrefreshed context.
        logger.info("Registering DispatcherServlet");
        DispatcherServlet dispatcherServlet = new DispatcherServlet(springContext);
        ServletHolder servletHolder = new ServletHolder("dispatcher", dispatcherServlet);
        servletContext.addServlet(servletHolder, "/*");

        server.setHandler(servletContext);

        // Start server
        try {
            logger.info("Starting Jetty server...");
            server.start();
            logger.info("✅ Jetty server started successfully on port: {}", port);
            logger.info("✅ Server is ready! Access it at: http://localhost:{}", port);
        } catch (Exception e) {
            logger.error("Failed to start Jetty server", e);
            throw e;
        }

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Shutting down Jetty server");
                server.stop();
                logger.info("Jetty server stopped");
            } catch (Exception e) {
                logger.error("Error during shutdown", e);
            }
        }));

        return server;
    }

    /**
     * Create Spring AnnotationConfigWebApplicationContext with property loading.
     * The context is intentionally left unrefreshed here - DispatcherServlet
     * refreshes it once a ServletContext has been attached (see createAndStartJettyServer).
     */
    private static AnnotationConfigWebApplicationContext createSpringContext(
            Class<?> configClass, String activeProfile) throws IOException {
        
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        
        // Set active profile BEFORE registering beans
        context.getEnvironment().setActiveProfiles(activeProfile);
        logger.info("Spring profile set to: {}", activeProfile);
        
        // Load properties
        loadProperties(context, activeProfile);
        
        // Register configuration classes
        context.register(configClass);
        context.register(SpringConfig.class);
        context.register(com.example.config.WebConfig.class);
        context.register(com.example.config.OpenApiConfig.class);
        
        // Register PropertySourcesPlaceholderConfigurer
        context.register(PropertySourcesPlaceholderConfigurer.class);
        
        return context;
    }

    /**
     * Load application properties from profile-specific file
     */
    private static void loadProperties(AnnotationConfigWebApplicationContext context, 
                                      String activeProfile) throws IOException {
        Properties props = new Properties();
        
        // Load default properties
        try {
            props.load(new ClassPathResource("application.properties").getInputStream());
        } catch (IOException e) {
            logger.warn("Default application.properties not found");
        }
        
        // Load profile-specific properties
        String profilePropsFile = "application-" + activeProfile + ".properties";
        try {
            props.load(new ClassPathResource(profilePropsFile).getInputStream());
            logger.info("Loaded properties from: {}", profilePropsFile);
        } catch (IOException e) {
            logger.warn("Profile-specific properties file not found: {}", profilePropsFile);
        }
        
        // Set properties in context
        context.getEnvironment().getPropertySources()
            .addFirst(new org.springframework.core.env.PropertiesPropertySource("application", props));
    }

    /**
     * Get server port from Spring context properties
     */
    private static int getServerPort(WebApplicationContext context) {
        String portStr = context.getEnvironment().getProperty("server.port");
        if (portStr != null && !portStr.isEmpty()) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid server port: {}, using default: {}", portStr, DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }

    /**
     * Custom servlet that serves static resources from the classpath
     */
    public static class ClasspathResourceServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.isEmpty()) {
                pathInfo = "/index.html";
            }
            
            // Prevent directory traversal
            if (pathInfo.contains("..") || pathInfo.contains("//")) {
                resp.setStatus(404);
                return;
            }
            
            try {
                // For /swagger-ui requests (exact path), redirect to versioned webjars path
                if (pathInfo.equals("/swagger-ui.html")) {
                    resp.sendRedirect("/webjars/swagger-ui/4.18.2/index.html");
                    return;
                }
                
                // Load resource from classpath
                String resourcePath = "META-INF/resources" + pathInfo;
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                java.net.URL resourceUrl = classLoader.getResource(resourcePath);
                
                if (resourceUrl == null) {
                    resp.setStatus(404);
                    return;
                }
                
                // Determine content type
                String contentType = "application/octet-stream";
                if (pathInfo.endsWith(".html")) {
                    contentType = "text/html";
                } else if (pathInfo.endsWith(".css")) {
                    contentType = "text/css";
                } else if (pathInfo.endsWith(".js")) {
                    contentType = "application/javascript";
                } else if (pathInfo.endsWith(".json")) {
                    contentType = "application/json";
                } else if (pathInfo.endsWith(".png")) {
                    contentType = "image/png";
                } else if (pathInfo.endsWith(".jpg") || pathInfo.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (pathInfo.endsWith(".svg")) {
                    contentType = "image/svg+xml";
                }
                
                resp.setContentType(contentType);
                
                // Copy resource content to response
                try (java.io.InputStream in = resourceUrl.openStream();
                     java.io.OutputStream out = resp.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error serving resource: {}", pathInfo, e);
                resp.setStatus(500);
            }
        }
    }

    /**
     * Filter to intercept and serve static resources from classpath before DispatcherServlet
     */
    public static class StaticResourceFilter implements Filter {
        private static final java.util.Set<String> STATIC_EXTENSIONS = java.util.Set.of(
            ".html", ".css", ".js", ".png", ".jpg", ".jpeg", ".svg", ".ico", ".woff", ".woff2", ".ttf", ".eot"
        );

        @Override
        public void init(FilterConfig config) throws ServletException {
        }

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                           FilterChain chain) throws java.io.IOException, ServletException {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletResponse httpResp = (HttpServletResponse) response;
            String path = httpReq.getRequestURI();
            String contextPath = httpReq.getContextPath();
            if (contextPath != null && contextPath.length() > 0) {
                path = path.substring(contextPath.length());
            }

            // Check if this is a static resource
            boolean isStatic = path.startsWith("/swagger-ui") ||
                             path.startsWith("/webjars/") ||
                             STATIC_EXTENSIONS.stream().anyMatch(path::endsWith);

            if (isStatic) {
                // Serve from classpath
                serveStaticResource(path, httpResp);
                return;
            }

            // Let other filters/servlets handle it
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }

        private void serveStaticResource(String path, HttpServletResponse resp) throws java.io.IOException {
            // Handle /swagger-ui.html redirect
            if (path.equals("/swagger-ui.html")) {
                resp.sendRedirect("/webjars/swagger-ui/4.18.2/index.html");
                return;
            }

            // Override the webjar's bundled swagger-initializer.js (which points to the
            // Swagger Petstore demo) so Swagger UI loads this application's own OpenAPI
            // document from /v3/api-docs instead.
            if (path.endsWith("/swagger-initializer.js")) {
                resp.setContentType("application/javascript");
                resp.getWriter().write(
                    "window.onload = function() {\n" +
                    "  window.ui = SwaggerUIBundle({\n" +
                    "    url: \"/v3/api-docs\",\n" +
                    "    dom_id: '#swagger-ui',\n" +
                    "    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],\n" +
                    "    plugins: [SwaggerUIBundle.plugins.DownloadUrl],\n" +
                    "    layout: \"StandaloneLayout\"\n" +
                    "  });\n" +
                    "};\n");
                return;
            }

            // Load from classpath
            String resourcePath = "META-INF/resources" + path;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            java.net.URL resourceUrl = classLoader.getResource(resourcePath);

            if (resourceUrl == null) {
                resp.setStatus(404);
                return;
            }

            // Set content type
            String contentType = "application/octet-stream";
            if (path.endsWith(".html")) contentType = "text/html";
            else if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".json")) contentType = "application/json";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            resp.setContentType(contentType);

            // Serve file
            try (java.io.InputStream in = resourceUrl.openStream();
                 java.io.OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                logger.warn("Error serving static resource: {}", path, e);
                resp.setStatus(500);
            }
        }
    }
}
