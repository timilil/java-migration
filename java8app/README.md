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
│   │   │   │   ├── SpringConfig.java         # Core Spring beans (datasource, liquibase, JPA)
│   │   │   │   ├── JpaConfig.java            # JPA repositories (dev/prod/integration-test)
│   │   │   │   ├── AppConfig.java            # RestTemplate/HttpClient/ObjectMapper beans
│   │   │   │   └── H2CompatibleDialect.java  # H2Dialect fix for identity-insert on H2 2.x
│   │   │   ├── controller/                   # REST controllers
│   │   │   ├── dto/                          # DTOs, incl. ExternalTodoDTO (Jackson mapping)
│   │   │   ├── entity/                       # JPA entities, incl. SharePermission enum
│   │   │   ├── repository/                   # Data access repositories
│   │   │   └── service/                      # Business logic services, incl. ExternalTodoClient
│   │   └── resources/
│   │       ├── application.properties         # Default/shared properties
│   │       ├── application-dev.properties     # Development profile (PostgreSQL + Liquibase)
│   │       ├── application-prod.properties    # Production profile (PostgreSQL with Liquibase)
│   │       ├── application-test.properties    # Test profile
│   │       ├── application-integration-test.properties  # Integration test profile (H2)
│   │       └── db/changelog/                 # Liquibase migrations, context-scoped per DB
│   ├── test/
│   │   └── java/com/example/                 # Unit tests + JVM-local IntegrationTest classes
│   └── integration-test/
│       └── java/com/example/integration/     # Integration tests (H2, wired via build-helper-maven-plugin)
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
- **Apache HttpClient 4.x** - HTTP client (used by `ExternalTodoClient` to call an external JSON API and map the response to a DTO via Jackson)
- **C3P0 0.9.5.x** - Connection pooling (dev/prod)
- **H2 2.x** - In-memory database (test/integration-test only)
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
- Requires a PostgreSQL database (`jdbc:postgresql://localhost:5432/java8app_dev` by default) — DataSource, Liquibase, EntityManagerFactory and JPA repositories are all enabled, same as prod
- Liquibase runs with `spring.liquibase.contexts=application`
- Spring component scanning discovers all @Component, @Service, @Controller beans
- ✅ Application starts successfully once the database is reachable
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
# Unit tests only (IntegrationTest classes are excluded from surefire)
mvn test

# Full build incl. Failsafe integration tests (src/test + src/integration-test, H2-backed)
mvn verify
```

## Profiles

The application supports 4 configuration profiles:

| Profile | Database | Services/Controller | Repos | EntityMgr | Liquibase | Liquibase context | Use Case |
|---------|----------|-------------------|-------|-----------|-----------|--------------------|----------|
| **dev** (default) | PostgreSQL | ✅ | ✅ | ✅ | ✅ | `application` | Local development against a real Postgres DB |
| **prod** | PostgreSQL | ✅ | ✅ | ✅ | ✅ | `application` | Production deployment with full DB features |
| **test** | PostgreSQL | ✅ | ✅ | ✅ | ❌ | n/a | Test environment with real database |
| **integration-test** | H2 (mem) | ✅ | ✅ | ✅ | ✅ | `integration-test` | Integration testing with H2 database |

Liquibase changesets can be scoped with a `context` attribute (e.g. `context="application"` vs `context="integration-test"`) so different DDL/data runs depending on which database is active. The context is driven by `spring.liquibase.contexts` in each `application-{profile}.properties` file, and is resolved before JPA validates the schema (`entityManagerFactory` bean `@DependsOn("liquibase")`).

## Configuration Files

### application.properties
```properties
spring.application.name=java8-spring-app
server.port=8080
logging.level.root=INFO
```

### application-dev.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/java8app_dev
spring.datasource.username=dev_user
spring.datasource.password=dev_password
spring.liquibase.enabled=true
spring.liquibase.contexts=application
logging.level.com.example=DEBUG
logging.level.org.springframework.web=DEBUG
```

### application-integration-test.properties
```properties
spring.datasource.url=jdbc:h2:mem:...
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=com.example.config.H2CompatibleDialect
spring.liquibase.contexts=integration-test
# DataSource, EntityManagerFactory, TransactionManager, Liquibase created (H2)
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

1. `Application.main()` detects active profile (defaults to `dev`; also parses `--spring.profiles.active=X` manually, since there's no Spring Boot `ApplicationArguments`)
2. Calls `createAndStartJettyServer(profile)`
3. Creates `AnnotationConfigWebApplicationContext` with active profile set
4. Loads `application.properties` and `application-{profile}.properties`
5. Registers configuration classes:
   - `SpringConfig`, `AppConfig` (always registered)
   - `JpaConfig` (profiles `dev`, `prod`, `integration-test`)
   - Other Spring components
6. Spring context is refreshed:
   - For **dev/prod**: Creates database beans (DataSource via C3P0, Liquibase, EntityManagerFactory) against PostgreSQL
   - For **integration-test**: Creates database beans against H2 (`DriverManagerDataSource` + `H2CompatibleDialect`)
7. Creates Jetty server with servlet context
8. Registers `DispatcherServlet` mapping to `/*`
9. Starts Jetty server on configured port
10. Application keeps running (awaits shutdown signal)

### Spring Configuration

**SpringConfig.java** (always loaded):
- DataSource bean (C3P0) for `dev`/`prod`, plain `DriverManagerDataSource` for `integration-test`
- Liquibase bean for `dev`/`prod`/`integration-test`, contexts resolved from `spring.liquibase.contexts`
- EntityManagerFactory `@DependsOn("liquibase")` so migrations run before schema validation
- TransactionManager setup

**JpaConfig.java** (`dev`/`prod`/`integration-test` profiles):
- Enables `@EnableJpaRepositories`
- Repository auto-scanning

**AppConfig.java** (always loaded):
- `RestTemplate`, `CloseableHttpClient` and Jackson `ObjectMapper` beans used by `ExternalTodoClient`

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
- postgresql.version = 42.6.0
- liquibase.version = 3.10.3
- h2.version = 2.1.214
- httpclient.version = 4.5.14
- jetty.version = 9.4.52.v20230823
- And more version properties...

**Zero hardcoded versions in dependencies** - all use `${property}` references.

## Feature Highlights

- **External HTTP client + JSON DTO mapping**: `ExternalTodoClient` uses Apache `CloseableHttpClient` to call an external REST API and Jackson `ObjectMapper` to deserialize the response into `ExternalTodoDTO`. Covered by `ExternalTodoClientTest` (unit, mocked) and `ExternalTodoClientIntegrationTest` (raw-socket local HTTP server).
- **Context-scoped Liquibase changesets**: changesets can be tagged `context="application"` (PostgreSQL) or `context="integration-test"` (H2) so different DDL runs per database. Verified by `LiquibaseContextIntegrationTest` and `ApplicationDatabaseStartupIntegrationTest`.
- **Datasource configuration tests**: `DataSourceConfigurationTest` validates both the PostgreSQL/C3P0 `dataSource()` bean and the H2 `integrationTestDataSource()` bean.
- **Enum persisted to DB**: `DocumentShare.permissionType` is a `SharePermission` enum (`VIEW`/`EDIT`/`ADMIN`) mapped with `@Enumerated(EnumType.STRING)`.
- **`GenerationType.AUTO` + latest-by-id lookup**: `DocumentAccessLog` uses `GenerationType.AUTO` with an explicit `@SequenceGenerator`. `DocumentService.getLatestAccessLogById()` fetches the highest-ID row via `findFirstByOrderByIdDesc()`.

## Known Issues & Notes

1. **H2 identity-insert compatibility**: H2 2.x rejects an explicit `NULL` value for `AUTO_INCREMENT`/identity columns, but Hibernate 5.4's `H2Dialect` still generates `NULL` in the identity-insert SQL. `H2CompatibleDialect` (extends `H2Dialect`) overrides `getIdentityColumnSupport()` to emit `default` instead, and is used wherever the `integration-test` profile configures Hibernate.

2. **Some pre-existing `src/integration-test/java` classes are excluded from Failsafe**: `DocumentCollaborationIntegrationTest`, `DocumentControllerIntegrationTest`, `DocumentRepositoryIntegrationTest`, `DocumentSharingIntegrationTest`, and `DocumentAuditIntegrationTest` are excluded in `pom.xml` because they fail for reasons unrelated to the features above (H2 2.x boolean-literal SQL strictness, and seed SQL scripts inserting hardcoded PKs that collide with the identity sequence).

3. **Startup Time**: Application starts quickly (~3 seconds), longer if the database connection is slow to establish.
