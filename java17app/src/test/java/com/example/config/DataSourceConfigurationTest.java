package com.example.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertNotNull;
    import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataSourceConfigurationTest {

    @Test
    public void productionDataSourceUsesConfiguredPostgresProperties() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/sample")
                .withProperty("spring.datasource.username", "sample_user")
                .withProperty("spring.datasource.password", "sample_password")
                .withProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

        DataSource dataSource = new SpringConfig().dataSource(environment);
        assertTrue(dataSource instanceof HikariDataSource);
        try (HikariDataSource pool = (HikariDataSource) dataSource) {
            assertEquals("jdbc:postgresql://localhost:5432/sample", pool.getJdbcUrl());
            assertEquals("sample_user", pool.getUsername());
        }
    }

    @Test
    public void integrationDataSourceConnectsToConfiguredH2Database() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:h2:mem:datasource-config;DB_CLOSE_DELAY=-1")
                .withProperty("spring.datasource.username", "sa")
                .withProperty("spring.datasource.password", "")
                .withProperty("spring.datasource.driver-class-name", "org.h2.Driver");

        DataSource dataSource = new SpringConfig().integrationTestDataSource(environment);
        assertTrue(dataSource instanceof DriverManagerDataSource);
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertEquals("H2", connection.getMetaData().getDatabaseProductName());
        }
    }
}