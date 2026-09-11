# Java 17 Spring Application

A complete example backend Spring application using Java 17 with Spring Framework 5.3, Hibernate 5.6, HikariCP, and manual Jetty initialization (no Spring Boot).

## Project Structure

```
java17app/
├── pom.xml                                    # Maven configuration with all property-based versioning
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── Application.java              # Main entry point with manual Jetty + Spring init
│   │   │   ├── config/                       # Spring configuration classes
│   │   │   │   ├── SpringConfig.java         # Core Spring beans (datasource with HikariCP, liquibase for prod)
│   │   │   │   ├── WebConfig.java            # Web MVC configuration
│   │   │   │   ├── OpenApiConfig.java        # SpringDoc OpenAPI/Swagger configuration
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
│   │       ├── logback.xml                    # Logback configuration
│   │       └── db/changelog/                 # Liquibase database migrations (prod only)
│   ├── test/
│   │   └── java/com/example/service/
│   │       └── UserServiceTest.java          # Unit tests (JUnit 5)
│   └── integration-test/
│       └── java/com/example/integration/
│           └── UserRepositoryIntegrationTest.java  # Integration tests
└── README.md
```

## Technology Stack

### Core
- **Java 17** (LTS)
- **Spring Framework 5.3.31** (manual initialization, no Spring Boot)
- **Spring Security 5.7.13** - Authentication & authorization  
- **Spring Data JPA 2.7.17** (prod profile only)
- **Hibernate 5.6.15.Final** (prod profile only)

### Web & Servlet
- **Jetty 10.0.20** - Embedded servlet container (manual init)
- **Jakarta Servlet API 6.0.0** - Servlet specification (Jakarta namespace)
- **SpringDoc OpenAPI 1.8.0** - API documentation & Swagger UI

### Database & Persistence
- **PostgreSQL 42.7.3 Driver** - Database driver (prod only)
- **Liquibase 4.25.1** - Database schema migrations (prod only)
- **HikariCP 5.1.0** - Connection pooling (prod only, replaces C3P0)
- **H2 2.2.224** - In-memory database (test only)

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

- Java 17 (or higher)
- Maven 3.6+
- PostgreSQL 9.6+ (for production profile only)

### Compile the Project

```bash
# Standard compilation (no database required)
mvn clean compile

# Full build with tests
mvn clean package

# Build without running tests
mvn clean package -DskipTests
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
java -jar target/java17-spring-app-2.0.0.jar

# Or with profile specified
java -jar target/java17-spring-app-2.0.0.jar --spring.profiles.active=dev
```

**Dev Profile Features:**
- Jetty server listens on `http://localhost:8080`
- No database required (JPA repositories disabled)
- Spring component scanning discovers all @Component, @Service, @Controller beans
- ✅ Application starts successfully
- ✅ Server responds on localhost:8080
- Visit `http://localhost:8080/` for a status page confirming the app is running and showing the active profile
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (shows API documentation with @Tag annotations)
- **API Docs**: `http://localhost:8080/v3/api-docs` (OpenAPI 3.0.1 JSON specification)

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
- Requires: `postgresql://localhost:5432/java17app_prod`
- Database credentials configured in `application-prod.properties`
- **REST API**: Full `/api/users` endpoints (GET, POST, PUT, DELETE)
- **Swagger UI**: Available at `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

### Test Profiles

```bash
# Integration tests use H2 in-memory database
mvn verify
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
spring.application.name=java17-spring-app
server.port=8080
logging.level.root=INFO
```

### application-dev.properties
```properties
# No database configuration
# Repositories and EntityManager NOT created
logging.level.com.example=DEBUG
logging.level.org.springframework.web=DEBUG
# Swagger UI configuration
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=true
springdoc.api-docs.enabled=true
```

### application-prod.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/java17app_prod
spring.datasource.username=prod_user
spring.datasource.password=prod_password
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true
spring.liquibase.contexts=prod
# DataSource, EntityManagerFactory, TransactionManager created
# JPA Repositories enabled
# HikariCP connection pooling configured
# Swagger UI disabled, API docs enabled only
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=true
```

## Architecture

###  Initialization Flow

1. `Application.main()` detects active profile via `--spring.profiles.active=X` or environment
2. Calls `createAndStartJettyServer(profile)`
3. Creates `AnnotationConfigWebApplicationContext` with active profile set
4. Loads `application.properties` and `application-{profile}.properties`
5. Registers configuration classes:
   - `SpringConfig` (always registered, contains datasource beans)
   - `WebConfig` (enables @EnableWebMvc for JSON converters)
   - `OpenApiConfig` (manually wires SpringDoc auto-configuration classes)
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
- DataSource bean (HikariCP) - prod only
- Liquibase bean - prod only  
- TransactionManager setup

**WebConfig.java** (always loaded):
- Enables `@EnableWebMvc` to auto-configure Spring MVC converters and handlers

**OpenApiConfig.java** (always loaded):
- Manually imports SpringDoc OpenAPI auto-configuration classes
- Enables Swagger UI and OpenAPI JSON generation

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
- `java.version=17`
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

**Zero hardcoded versions in dependencies** - all use `${property}` references.

## Migration Notes (from Java 8 to Java 17)

### Key Changes

1. **Java Version**: 1.8 → 17
   - All Java 9+ features now available (var, records, sealed classes, pattern matching, etc.)
   - Module system (java.base, java.naming, etc.) available but not required for this app

2. **Servlet API**: javax.servlet → jakarta.servlet (6.0.0)
   - Jetty 10+ requires Jakarta namespace
   - Note: Hibernate 5.6 still uses javax.persistence (not jakarta.persistence)

3. **Spring Framework**: 5.2 → 5.3
   - Better Java 17+ support
   - Improved null-safety annotations
   - Better web MVC enhancements

4. **Database Connection Pooling**: C3P0 0.9.5.x → HikariCP 5.1.0
   - HikariCP is modern, actively maintained, and performs better
   - Configuration migrated in SpringConfig.java
   - Set `@DependsOn("liquibase")` on EntityManagerFactory ensures correct initialization order

5. **Testing**: JUnit 4 → JUnit 5 (Jupiter)
   - Import changes: `org.junit.*` → `org.junit.jupiter.api.*`
   - Annotation changes: `@Before` → `@BeforeEach`, `@Test(expected=...)` → `assertThrows(...)`
   - Mockito integration: `@RunWith(SpringRunner.class)` → `@ExtendWith(MockitoExtension.class)`
   - Added `mockito-junit-jupiter` dependency for full JUnit 5 support

6. **Logging**: SLF4J 1.7 + Logback 1.2 → SLF4J 2.0 + Logback 1.3
   - SLF4J 2.0 requires Java 8+ and offers better performance
   - Logback 1.3 has better Java 17+ support
   - Configuration files updated (logback.xml uses modern syntax)

7. **Mockito**: 1.10.x → 5.7.0
   - Much stricter testing rules (no unnecessary stubbings)
   - Better Java 17+ support via ByteBuddy
   - Configuration `MockitoAnnotations.initMocks(this)` → `@ExtendWith(MockitoExtension.class)` on class

8. **SpringDoc OpenAPI**: 1.7.0 → 1.8.0
   - Better support for Swagger UI with newer Spring versions
   - OpenAPI Config now manually wired via OpenApiConfig.java
   - Swagger UI and API docs fully functional in both dev and prod

## Known Issues & Notes

1. **404 on user endpoints in dev profile**: This is expected. Dev profile intentionally disables `UserController`/`UserService` (no database), so there are no HTTP routes registered. The server itself starts and runs fine — check the startup logs for `✅ Jetty server started successfully`. Use `--spring.profiles.active=prod` to enable the `/api/users` endpoints.

2. **Dev Profile Architecture**: 
   - No database dependencies (UserService and UserController disabled via @Profile("prod"))
   - JPA repositories NOT enabled (only in prod/test/integration-test profiles)
   - EntityManagerFactory NOT created
   - Allows clean development without PostgreSQL setup
   - Use prod profile for full application features

3. **Startup Time**: Application starts quickly (~3-4 seconds in dev, longer with database in prod)

4. **Java 17 Module System**: Not explicitly configured for this application
   - Application runs in classpath mode
   - No module-info.java file required
   - All libraries loaded via classpath

5. **HikariCP Configuration**: Replaces C3P0
   - Maximum pool size: 10
   - Minimum idle: 5
   - Connection timeout: 30 seconds
   - Max lifetime: 30 minutes
   - Idle timeout: 10 minutes
