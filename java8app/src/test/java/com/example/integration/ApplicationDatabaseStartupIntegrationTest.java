package com.example.integration;

import com.example.config.JpaConfig;
import com.example.config.SpringConfig;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ApplicationDatabaseStartupIntegrationTest {

    @Test
    public void integrationTestProfileStartsLiquibaseBeforeJpa() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("integration-test");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("spring.datasource.url", "jdbc:h2:mem:app_startup;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        properties.put("spring.datasource.username", "sa");
        properties.put("spring.datasource.password", "");
        properties.put("spring.datasource.driver-class-name", "org.h2.Driver");
        properties.put("spring.liquibase.contexts", "integration-test");
        properties.put("spring.jpa.database-platform", "com.example.config.H2CompatibleDialect");
        properties.put("spring.jpa.hibernate.ddl-auto", "validate");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
        context.register(SpringConfig.class, JpaConfig.class);

        try {
            context.refresh();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(context.getBean(DataSource.class));
            assertEquals("integration-test", jdbcTemplate.queryForObject(
                    "SELECT context_name FROM integration_context_marker WHERE id = 1", String.class));
        } finally {
            context.close();
        }
    }
}