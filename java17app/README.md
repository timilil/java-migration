# Java 17 Spring Application

A complete example backend Spring application using Java 17 with Spring Framework 5.3, Hibernate 5.6, HikariCP, and manual Jetty initialization (no Spring Boot).

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── Application.java       # Manual Jetty and Spring bootstrap
│   │   ├── config/                # Spring, JPA, security, web, and OpenAPI configuration
│   │   ├── controller/            # MVC and REST controllers
│   │   ├── dto/                   # API data transfer objects
│   │   ├── entity/                # JPA entities
│   │   ├── repository/            # Spring Data repositories
│   │   └── service/               # Business services
│   └── resources/                 # Properties, Liquibase changelogs, static UI, logging
├── test/java/                     # Unit and service tests
└── integration-test/              # H2-backed integration tests
```

## Technology Stack

### Core
- **Java 17** (LTS)
- **Spring Framework 5.3.31** (manual initialization, no Spring Boot)
- **Spring Security 5.7.13** - Authentication & authorization  
- **Spring Data JPA 2.7.17** (database-backed profiles)
- **Hibernate 5.6.15.Final** (database-backed profiles)

### Web & Servlet
- **Jetty 10.0.20** - Embedded servlet container (manual init)
- **Jakarta Servlet API 6.0.0** - Declared in the POM; see the namespace compatibility note below
- **SpringDoc OpenAPI 1.8.0** - API documentation & Swagger UI

### Database & Persistence
- **PostgreSQL 42.7.3 Driver** - Database driver (`dev` and `prod`)
- **Liquibase 4.25.1** - Database schema migrations (database-backed profiles)
- **HikariCP 5.1.0** - Connection pooling (`dev` and `prod`; integration tests use `DriverManagerDataSource`)
- **H2 2.2.224** - In-memory integration-test database

### Serialization & HTTP
- **Jackson 2.18.0** - JSON processing
- **Apache HttpClient 5.4** - HTTP client

### Testing
- **JUnit 5.10.1** (Jupiter) - Unit testing framework
- **Mockito 5.7.0** - Mocking framework
- **Mockito JUnit Jupiter 5.7.0** - JUnit 5 integration for Mockito

### Logging
- **SLF4J 2.0.13** - Logging facade
- **Logback 1.3.14** - Logging implementation

## Building the Project

### Prerequisites

- Java 17
- Maven 3.6+
- PostgreSQL (for the `dev` and `prod` runtime profiles; integration tests use H2)

### Compile the Project

```bash
# Standard compilation (no database required)
mvn clean compile

# Unit tests plus integration tests (see the Failsafe note below)
mvn clean verify -Dfailsafe.useModulePath=false

# Build without running tests
mvn clean package -DskipTests
```

### Run Tests

```bash
# Run all unit tests
mvn test

# Run unit and integration tests
mvn verify -Dfailsafe.useModulePath=false

# Run specific test
mvn test -Dtest=UserServiceTest
```

## Running the Application

### Development Profile (default; PostgreSQL required)

```bash
# Build and run
mvn clean package
java -jar target/java17-spring-app-2.0.0.jar

# Or with profile specified
java -jar target/java17-spring-app-2.0.0.jar --spring.profiles.active=dev
```

**Dev Profile Features:**
- Jetty server listens on `http://localhost:8080`
- PostgreSQL connection at `jdbc:postgresql://localhost:5432/java8app_dev`
- HikariCP, Liquibase, EntityManagerFactory, transaction manager, and JPA repositories are enabled
- `UserController`, `UserService`, and `DocumentController` are production-profile-only, so their endpoints are not registered in `dev`
- Spring component scanning discovers all @Component, @Service, @Controller beans
- Unauthenticated requests to `/` are redirected to `/login`
- SpringDoc is manually wired because the application does not use Spring Boot; its configuration-properties binding is not enabled

### Production Profile (PostgreSQL Required)

```bash
java -jar target/java17-spring-app-2.0.0.jar --spring.profiles.active=prod
```

**Prod Profile Features:**
- PostgreSQL database connection (HikariCP connection pool, 5-10 connections)
- Liquibase database migrations auto-run
- JPA repositories enabled
- EntityManagerFactory created
- TransactionManager configured
- Requires: `jdbc:postgresql://localhost:5432/java8app_prod`
- Database credentials configured in `application-prod.properties`
- **REST API**: Full `/api/users` endpoints (GET, POST, PUT, DELETE)
- Swagger UI/API-doc behavior follows the manually imported SpringDoc defaults; profile-specific `springdoc.*.enabled` properties are not bound by this bootstrap

### Test Profiles

```bash
# Integration tests use an H2 in-memory database
mvn clean verify -Dfailsafe.useModulePath=false
```

## Profiles

The application has four Maven profiles (currently empty) and four Spring profile property files. The runtime Spring profile is selected from `--spring.profiles.active=...`, the `spring.profiles.active` system property, the `SPRING_PROFILES_ACTIVE` environment variable, or defaults to `dev`. Maven `-P...` does not select the runtime profile.

| Spring profile | Database configuration | Database/JPA beans active | Notes |
|---------------|------------------------|--------------------------|-------|
| **dev** (default) | PostgreSQL (`java8app_dev`) | Yes | HikariCP and Liquibase are enabled; user/document management controllers are not active |
| **prod** | PostgreSQL (`java8app_prod`) | Yes | HikariCP, Liquibase, JPA, and production-profile controllers/services |
| **test** | PostgreSQL settings exist in its properties file | No matching database/JPA `@Profile` configuration | Not the profile used by the Maven unit tests |
| **integration-test** | H2 in-memory | Yes | Used by the integration-test Spring context |

## Configuration Files

The property files contain legacy `java8-spring-app` names and `java8app_*` database names despite this being the Java 17 project. The application loads `application.properties` and then the selected `application-{profile}.properties` file itself; this is not Spring Boot property auto-configuration.

### application.properties
```properties
spring.application.name=java8-spring-app
spring.profiles.default=dev
logging.level.root=INFO
```

### application-dev.properties
```properties
# PostgreSQL configuration is present and consumed by SpringConfig
spring.datasource.url=jdbc:postgresql://localhost:5432/java8app_dev
spring.datasource.username=dev_user
spring.datasource.password=dev_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.liquibase.contexts=application
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
spring.liquibase.contexts=application
# DataSource, EntityManagerFactory, TransactionManager created
# JPA Repositories enabled
# HikariCP connection pooling configured
```

## Architecture

###  Initialization Flow

1. `Application.main()` detects the active profile from a command-line argument, system property, or environment variable; it defaults to `dev`
2. Calls `createAndStartJettyServer(profile)`
3. Creates `AnnotationConfigWebApplicationContext` with active profile set
4. Loads `application.properties` and `application-{profile}.properties`
5. Registers configuration classes:
   - `SpringConfig`, `WebConfig`, and `OpenApiConfig`
   - Other `com.example` components, including profile-conditional `JpaConfig`
6. Spring context is refreshed:
   - For **dev** and **prod**: Creates PostgreSQL/HikariCP, Liquibase, JPA, and transaction beans
   - For **integration-test**: Creates H2, Liquibase, JPA, and transaction beans
7. Creates Jetty server with servlet context
8. Registers `DispatcherServlet` mapping to `/*`
9. Starts Jetty server on configured port
10. Application keeps running (awaits shutdown signal)

### Spring Configuration

**SpringConfig.java** (always registered; beans are profile-conditional):
- HikariCP DataSource for `dev` and `prod`; `DriverManagerDataSource` for `integration-test`
- Liquibase and EntityManagerFactory for `dev`, `prod`, and `integration-test`
- EntityManagerFactory depends on Liquibase so schema migrations run first
- JPA transaction manager for database-backed profiles

**WebConfig.java** (always loaded):
- Enables `@EnableWebMvc` to auto-configure Spring MVC converters and handlers

**OpenApiConfig.java** (always loaded):
- Manually imports SpringDoc configuration classes because Spring Boot auto-configuration is not running
- Does not enable Spring configuration-properties binding for SpringDoc; profile-specific property toggles should not be assumed effective

**JpaConfig.java** (`dev`, `prod`, and `integration-test` profiles):
- Enables `@EnableJpaRepositories` for database-backed profiles

**Application.java**:
- Manual Jetty initialization
- Spring context creation with profile support
- Property loading from classpath

## Dependency Management

**Dependency versions are defined as properties in pom.xml; the Java compiler level is configured directly as 17:**
- `spring.framework.version=5.3.31`
- `spring.security.version=5.7.13`
- `spring.data.jpa.version=2.7.17`
- `hibernate.version=5.6.15.Final`
- `jetty.version=10.0.20`
- `postgresql.version=42.7.3`
- `liquibase.version=4.25.1`
- `jackson.version=2.18.0`
- `junit.version=5.10.1`
- `mockito.version=5.7.0`
- `springdoc.openapi.version=1.8.0`
- `hikaricp.version=5.1.0`
- `h2.version=2.2.224`
- `slf4j.version=2.0.13`
- `logback.version=1.3.14`
- `httpclient.version=5.4`
- `servlet.api.version=6.0.0`

Dependency versions use `${property}` references. The POM does not define a `java.version` property.

## Migration Notes (from Java 8 to Java 17)
The current POM targets Java 17 and upgrades several libraries compared with the Java 8 project, including Spring Framework 5.2 → 5.3, Spring Security 5.3 → 5.7, Spring Data JPA 2.2 → 2.7, Hibernate 5.4 → 5.6, Jetty 9 → 10, Liquibase 3 → 4, HttpClient 4 → 5, JUnit 4 → 5, and Mockito 1 → 5. HikariCP is used instead of C3P0 in the Java 17 project.

**Important namespace compatibility note:** this is not a completed `javax` → `jakarta` migration. The POM declares `jakarta.servlet-api:6.0.0`, but Jetty 10 and application sources use the `javax.servlet` namespace; application sources also use `javax.persistence` with Hibernate 5.6. The declared Servlet API dependency and the APIs used by the application do not match. This README describes the current state; it does not resolve the dependency mismatch.

The application has no `module-info.java` and is compiled and run on the classpath. Targeting Java 17 makes newer language features available, but does not mean they have all been adopted in the source.

## Known Issues & Notes
1. **Dev profile requires PostgreSQL.** It enables database beans, while the user and document management controllers/services are limited to `prod` (the document service also participates in integration tests). Use `prod` for the production-profile REST APIs.

2. **Integration-test database compatibility:** `H2CompatibleDialect` customizes Hibernate 5.6's H2 identity-column SQL for H2 2.x. The integration-test Spring context uses this dialect with an in-memory H2 database.

3. **Failsafe module path:** on the current Java 17/Maven setup, plain `mvn verify` fails to start the integration-test fork because HikariCP is detected twice on the module path. Use `mvn verify -Dfailsafe.useModulePath=false` to run the integration suite without changing the POM.

4. **Demo security only:** `SecurityConfig` permits unauthenticated `/api/**` requests, and login compares the supplied password directly with the stored value. Do not deploy this example as-is or use real credentials.

5. **Servlet dependency mismatch:** see the namespace compatibility note above before treating the declared Jakarta Servlet API dependency as a completed migration.
