package com.example.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
 * Database beans (DataSource, EntityManagerFactory, TransactionManager) created for 'prod' and 'dev' profiles
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.repository")
public class SpringConfig {

    /**
     * Create C3P0 connection pool DataSource
     * Active for 'prod' profile with PostgreSQL, or 'dev' profile with H2
     */
    @Bean
    @Profile({"prod", "dev"})
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


    /**
     * Create Liquibase bean for database migrations
     * Active for 'prod' and 'dev' profiles
     */
    @Bean
    @Profile({"prod", "dev"})
    public SpringLiquibase liquibase(DataSource dataSource, Environment env) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        
        // Allow clearChecksums to be set via property for development environments
        // Usage: --spring.liquibase.clear-checksums=true
        String clearChecksums = env.getProperty("spring.liquibase.clear-checksums");
        if ("true".equalsIgnoreCase(clearChecksums)) {
            liquibase.setClearCheckSums(true);
        }
        
        String contexts = env.getProperty("spring.liquibase.contexts");
        if (contexts != null && !contexts.isEmpty()) {
            liquibase.setContexts(contexts);
        }
        
        return liquibase;
    }

    /**
     * Create EntityManagerFactory
     * For 'prod' and 'dev' profiles
     * Depends on Liquibase so schema migrations run before Hibernate validates the schema
     */
    @Bean
    @Profile({"prod", "dev"})
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
     * For 'prod' and 'dev' profiles
     */
    @Bean
    @Profile({"prod", "dev"})
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
