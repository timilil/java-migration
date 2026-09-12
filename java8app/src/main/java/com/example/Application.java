package com.example;

import com.example.config.SpringConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

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

        // Create servlet context
        ServletContextHandler servletContext = new ServletContextHandler();
        servletContext.setContextPath("/");

        // Add session management to support authentication
        DefaultSessionIdManager idManager = new DefaultSessionIdManager(server);
        server.setSessionIdManager(idManager);
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSessionIdManager(idManager);
        servletContext.setSessionHandler(sessionHandler);

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

        // Add servlet context to server
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
}
