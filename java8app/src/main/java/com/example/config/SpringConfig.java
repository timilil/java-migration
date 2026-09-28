package com.example.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;

/**
 * Spring Configuration - Manual bean definitions without Spring Boot auto-configuration
 * Database beans (DataSource, EntityManagerFactory, TransactionManager) only created for 'prod' profile
 */
@Configuration
@EnableTransactionManagement
public class SpringConfig {

    /**
     * Create C3P0 connection pool DataSource
     * Only active for 'prod' profile
     */
    @Bean
    @Profile({"dev", "prod"})
    public DataSource dataSource(Environment env) throws PropertyVetoException {
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        String driverClass = env.getProperty("spring.datasource.driver-class-name");

        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass(driverClass);
        cpds.setJdbcUrl(url);
        cpds.setUser(username);
        cpds.setPassword(password);
        cpds.setMaxPoolSize(10);
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(3);
        cpds.setMaxIdleTime(600);
        cpds.setMaxConnectionAge(1800);
        return cpds;
    }

    @Bean
    @Profile("integration-test")
    public DataSource integrationTestDataSource(Environment env) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name", "org.h2.Driver"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username", "sa"));
        dataSource.setPassword(env.getProperty("spring.datasource.password", ""));
        return dataSource;
    }


    /**
     * Create Liquibase bean for database migrations
     * Only active for 'prod' profile
     */
    @Bean
    @Profile({"dev", "prod", "integration-test"})
    public SpringLiquibase liquibase(DataSource dataSource, Environment env) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        
        String contexts = env.getProperty("spring.liquibase.contexts", "application");
        if (contexts != null && !contexts.isEmpty()) {
            liquibase.setContexts(contexts);
        }
        
        return liquibase;
    }

    /**
     * Create EntityManagerFactory
     * Only for 'prod' profile
     * Depends on Liquibase so schema migrations run before Hibernate validates the schema
     */
    @Bean
    @Profile({"dev", "prod", "integration-test"})
    @DependsOn("liquibase")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, Environment env) {
        
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.entity");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        // Hibernate properties
        java.util.Properties hibernateProps = new java.util.Properties();
        hibernateProps.setProperty("hibernate.dialect", 
            env.getProperty("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQL10Dialect"));
        hibernateProps.setProperty("hibernate.hbm2ddl.auto", 
            env.getProperty("spring.jpa.hibernate.ddl-auto", "validate"));
        hibernateProps.setProperty("hibernate.format_sql", 
            env.getProperty("spring.jpa.properties.hibernate.format_sql", "true"));
        hibernateProps.setProperty("hibernate.jdbc.batch_size", 
            env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size", "20"));
        hibernateProps.setProperty("hibernate.order_inserts", 
            env.getProperty("spring.jpa.properties.hibernate.order_inserts", "true"));
        hibernateProps.setProperty("hibernate.order_updates", 
            env.getProperty("spring.jpa.properties.hibernate.order_updates", "true"));
        
        emf.setJpaProperties(hibernateProps);
        return emf;
    }

    /**
     * Create Transaction Manager
     * Only for 'prod' profile
     */
    @Bean
    @Profile({"dev", "prod", "integration-test"})
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
