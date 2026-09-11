# Java 8 Spring Application

A complete example backend Spring application using Java 8 with Spring Framework 5.2, Hibernate 5.4, and manual Jetty initialization (no Spring Boot).

## Project Structure

```
java8app/
├── pom.xml                                    # Maven configuration with all property-based versioning
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── Application.java              # Main entry point with manual Jetty + Spring init
│   │   │   ├── config/                       # Spring configuration classes
│   │   │   │   ├── SpringConfig.java         # Core Spring beans (datasource, liquibase for prod)
│   │   │   │   └── JpaConfig.java            # JPA repositories (prod profile only)
│   │   │   ├── controller/                   # REST controllers
│   │   │   ├── entity/                       # JPA entities
│   │   │   ├── repository/                   # Data access repositories (prod profile only)
│   │   │   └── service/                      # Business logic services
│   │   └── resources/
│   │       ├── application.properties         # Default properties (dev profile)
│   │       ├── application-dev.properties     # Development profile (Jetty on 8080, no DB)
│   │       ├── application-prod.properties    # Production profile (PostgreSQL with Liquibase)
│   │       ├── application-test.properties    # Test profile
│   │       ├── application-integration-test.properties  # Integration test profile
│   │       └── db/changelog/                 # Liquibase database migrations (prod only)
│   ├── test/
│   │   └── java/com/example/service/
│   │       └── UserServiceTest.java          # Unit tests
│   └── integration-test/
│       └── java/com/example/integration/
│           └── UserRepositoryIntegrationTest.java  # Integration tests
└── README.md
```

## Technology Stack

- **Java 8**
- **Spring Framework 5.2.20.RELEASE** (manual initialization, no Spring Boot)
- **Spring Security 5.3.10.RELEASE** - Authentication & authorization  
- **Spring Data JPA 2.2.13.RELEASE** (prod profile only)
- **Hibernate 5.4.33.Final** (prod profile only)
- **Jetty 9.4.52.v20230823** - Embedded servlet container (manual init)
- **PostgreSQL 42.x Driver** - Database driver (prod only)
- **Liquibase 3.x** - Database schema migrations (prod only)
- **Jackson 2.x** - JSON processing
- **JUnit 4.x** - Unit testing
- **Mockito 1.x** - Mocking framework
- **SpringDoc OpenAPI 1.x** - API documentation
- **Apache HttpClient 4.x** - HTTP client
- **C3P0 0.9.5.x** - Connection pooling (prod only)
- **H2 2.x** - In-memory database (test only)
- **SLF4J & Logback** - Logging

## Building the Project

### Prerequisites

- Java 8 (or higher)
- Maven 3.6+
- PostgreSQL 9.6+ (for production profile only)

### Compile the Project

```bash
# Standard compilation (no database required)
mvn clean compile

# Full build with tests
mvn clean package
```

### Run Tests

```bash
# Run all unit tests
mvn test

# Run integration tests
mvn verify

# Run specific test
mvn test -Dtest=UserServiceTest
```

## Running the Application

### Development Profile (DEFAULT - No Database Required)

```bash
# Build and run
mvn clean package
java -jar target/java8-spring-app-1.0.0.jar

# Or with profile specified
java -jar target/java8-spring-app-1.0.0.jar --spring.profiles.active=dev
```

**Dev Profile Features:**
- Jetty server listens on `http://localhost:8080`
- No database required (JPA repositories disabled)
- Spring component scanning discovers all @Component, @Service, @Controller beans
- ✅ Application starts successfully
- ✅ Server responds on localhost:8080
- Visit `http://localhost:8080/` for a status page confirming the app is running and showing the active profile

### Production Profile (PostgreSQL Required)

```bash
java -jar target/java8-spring-app-1.0.0.jar --spring.profiles.active=prod
```

**Prod Profile Features:**
- PostgreSQL database connection (C3P0 connection pool)
- Liquibase database migrations auto-run
- JPA repositories enabled
- EntityManagerFactory created
- TransactionManager configured
- Requires: `postgresql://localhost:5432/java8app_prod`
- Database credentials configured in `application-prod.properties`

### Test Profiles

```bash
# Integration tests use H2 in-memory database
mvn verify -Pintegration-test
```

## Profiles

The application supports 4 configuration profiles:

| Profile | Database | Services/Controller | Repos | EntityMgr | Liquibase | Use Case |
|---------|----------|-------------------|-------|-----------|-----------|----------|
| **dev** (default) | None | ❌ | ❌ | ❌ | ❌ | Local development, no database dependencies |
| **prod** | PostgreSQL | ✅ | ✅ | ✅ | ✅ | Production deployment with full DB features |
| **test** | PostgreSQL | ✅ | ✅ | ✅ | ❌ | Test environment with real database |
| **integration-test** | H2 (mem) | ✅ | ✅ | ✅ | ❌ | Integration testing with H2 database |

## Configuration Files

### application.properties
```properties
spring.application.name=java8-spring-app
server.port=8080
logging.level.root=INFO
```

### application-dev.properties
```properties
# No database configuration
# Repositories and EntityManager NOT created
logging.level.com.example=DEBUG
logging.level.org.springframework.web=DEBUG
```

### application-prod.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/java8app_prod
spring.datasource.username=prod_user
spring.datasource.password=prod_password
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true
# DataSource, EntityManagerFactory, TransactionManager created
# JPA Repositories enabled
```

## Architecture

###  Initialization Flow

1. `Application.main()` detects active profile
2. Calls `createAndStartJettyServer(profile)`
3. Creates `AnnotationConfigWebApplicationContext` with active profile set
4. Loads `application.properties` and `application-{profile}.properties`
5. Registers configuration classes:
   - `SpringConfig` (always registered)
   - `JpaConfig` (only if prod profile)
   - Other Spring components
6. Spring context is refreshed:
   - For **dev**: Creates basic Spring beans, NO database beans
   - For **prod**: Creates database beans (DataSource, EntityManagerFactory, Liquibase)
7. Creates Jetty server with servlet context
8. Registers `DispatcherServlet` mapping to `/*`
9. Starts Jetty server on configured port
10. Application keeps running (awaits shutdown signal)

### Spring Configuration

**SpringConfig.java** (always loaded):
- DataSource bean (C3P0) - ✅ prod only
- Liquibase bean - ✅ prod only  
- TransactionManager setup

**JpaConfig.java** (prod profile only):
- Enables `@EnableJpaRepositories` for prod profile
- EntityManagerFactory bean
- Repository auto-scanning

**Application.java**:
- Manual Jetty initialization
- Spring context creation with profile support
- Property loading from classpath

## Dependency Management

**All versions defined as properties in pom.xml:**
- spring.framework.version = 5.2.20.RELEASE
- spring.security.version = 5.3.10.RELEASE
- spring.data.jpa.version = 2.2.13.RELEASE
- hibernate.version = 5.4.33.Final
- postgresql.version = 42.7.1
- jetty.version = 9.4.52.v20230823
- And 17 more version properties...

**Zero hardcoded versions in dependencies** - all use `${property}` references.

## Known Issues & Notes

1. **404 on user endpoints in dev profile**: This is expected. Dev profile intentionally disables `UserController`/`UserService` (no database), so there are no HTTP routes registered. The server itself starts and runs fine — check the startup logs for `✅ Jetty server started successfully`. Use `--spring.profiles.active=prod` to enable the `/api/users` endpoints.

2. **Dev Profile Architecture**: 
   - No database dependencies (UserService and UserController disabled via @Profile("prod"))
   - JPA repositories NOT enabled (only in prod/test/integration-test profiles)
   - EntityManagerFactory NOT created
   - Allows clean development without PostgreSQL setup
   - Use prod profile for full application features

3. **Startup Time**: Application starts quickly (~3 seconds in dev, longer with database in prod)
