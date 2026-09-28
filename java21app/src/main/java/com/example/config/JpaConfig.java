package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration - Only enabled for 'prod' profile
 * Enables repositories and transaction management
 */
@Configuration
@Profile({"dev", "prod", "integration-test"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.repository")
public class JpaConfig {
}
