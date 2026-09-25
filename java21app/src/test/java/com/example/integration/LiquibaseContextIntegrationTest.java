package com.example.integration;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LiquibaseContextIntegrationTest {

    @Test
    public void integrationTestContextRunsItsChangesetsOnH2() throws Exception {
        DataSource dataSource = h2DataSource("liquibase_it_context");
        runLiquibase(dataSource, "integration-test");

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertEquals("integration-test", jdbc.queryForObject(
                "SELECT context_name FROM integration_context_marker WHERE id = 1", String.class));
        assertFalse(tableExists(dataSource, "application_context_marker"));
    }

    @Test
    public void applicationContextRunsItsChangesets() throws Exception {
        DataSource dataSource = h2DataSource("liquibase_application_context");
        runLiquibase(dataSource, "application");

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertEquals("application", jdbc.queryForObject(
                "SELECT context_name FROM application_context_marker WHERE id = 1", String.class));
        assertFalse(tableExists(dataSource, "integration_context_marker"));
    }

    private DataSource h2DataSource(String databaseName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private void runLiquibase(DataSource dataSource, String contexts) throws Exception {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquibase.setContexts(contexts);
        liquibase.setResourceLoader(new DefaultResourceLoader());
        liquibase.afterPropertiesSet();
    }

    private boolean tableExists(DataSource dataSource, String tableName) throws Exception {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet result = metadata.getTables(null, null, tableName.toUpperCase(), new String[] {"TABLE"})) {
                return result.next();
            }
        }
    }
}