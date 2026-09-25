package com.example.integration;

import com.example.entity.Document;
import com.example.entity.DocumentAccessLog;
import com.example.entity.DocumentShare;
import com.example.entity.User;
import com.example.repository.DocumentAccessLogRepository;
import com.example.repository.DocumentRepository;
import com.example.repository.DocumentShareRepository;
import com.example.repository.UserRepository;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DocumentPersistenceIntegrationTest.PersistenceConfig.class)
@Transactional
public class DocumentPersistenceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentShareRepository documentShareRepository;

    @Autowired
    private DocumentAccessLogRepository documentAccessLogRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void h2UsersIdColumnIsAnIdentity() {
        assertEquals("YES", jdbcTemplate.queryForObject(
                "SELECT IS_IDENTITY FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'USERS' AND COLUMN_NAME = 'ID'",
                String.class));
    }

    @Test
    public void sharePermissionEnumIsPersistedAsItsName() {
        User owner = userRepository.save(new User("enum_owner", "enum-owner@example.com", "pw"));
        User recipient = userRepository.save(new User("enum_recipient", "enum-recipient@example.com", "pw"));
        Document document = documentRepository.save(new Document("Enum test", owner.getId(), "text/plain"));
        DocumentShare share = documentShareRepository.saveAndFlush(
                new DocumentShare(document.getId(), owner.getId(), recipient.getId(), "EDIT"));

        entityManager.clear();

        assertEquals("EDIT", documentShareRepository.findById(share.getId()).get().getPermissionType());
        assertEquals("EDIT", jdbcTemplate.queryForObject(
                "SELECT permission_type FROM document_shares WHERE id = ?", String.class, share.getId()));
    }

    @Test
    public void latestAccessLogLookupReturnsMostRecentlyCreatedAuditRow() {
        User owner = userRepository.save(new User("latest_owner", "latest-owner@example.com", "pw"));
        Document document = documentRepository.save(new Document("Latest test", owner.getId(), "text/plain"));
        DocumentAccessLog earlier = new DocumentAccessLog(document.getId(), owner.getId(), "VIEW");
        DocumentAccessLog later = new DocumentAccessLog(document.getId(), owner.getId(), "EDIT");
        documentAccessLogRepository.saveAndFlush(earlier);
        documentAccessLogRepository.saveAndFlush(later);

        DocumentAccessLog latest = documentAccessLogRepository.findFirstByOrderByIdDesc();

        assertNotNull(latest);
        assertEquals(later.getId(), latest.getId());
        assertEquals("EDIT", latest.getActionType());
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackages = "com.example.repository")
    public static class PersistenceConfig {

        @Bean
        public DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:persistence_features;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public SpringLiquibase liquibase(DataSource dataSource) {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
            liquibase.setContexts("integration-test");
            return liquibase;
        }

        @Bean
        @DependsOn("liquibase")
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.example.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            Properties properties = new Properties();
            properties.setProperty("hibernate.dialect", "com.example.config.H2CompatibleDialect");
            properties.setProperty("hibernate.hbm2ddl.auto", "validate");
            factory.setJpaProperties(properties);
            return factory;
        }

        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }
}