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
│   │   │   │   ├── JpaConfig.java            # JPA repositories (prod profile only)
│   │   │   ├── WebConfig.java                # Web MVC config with static resource handlers
│   │   │   ├── controller/                   # REST controllers
│   │   │   │   ├── StatusController.java     # Serves home page UI at root path
│   │   │   │   ├── UserController.java       # User CRUD & search endpoints
│   │   │   │   └── DocumentController.java   # Document management & collaboration endpoints
│   │   │   ├── entity/                       # JPA entities
│   │   │   ├── repository/                   # Data access repositories (prod profile only)
│   │   │   └── service/                      # Business logic services
│   │   └── resources/
│   │       ├── application.properties         # Default properties (dev profile)
│   │       ├── application-dev.properties     # Development profile (Jetty on 8080, no DB)
│   │       ├── application-prod.properties    # Production profile (PostgreSQL with Liquibase)
│   │       ├── application-test.properties    # Test profile
│   │       ├── application-integration-test.properties  # Integration test profile
│   │       ├── static/
│   │       │   ├── pages/                    # Feature-specific pages
│   │       │   │   ├── users.html            # User management (CRUD operations)
│   │       │   │   ├── documents.html        # Document management (CRUD + search)
│   │       │   │   ├── sharing.html          # Document sharing (permissions, access control)
│   │       │   │   ├── categories.html       # Category management
│   │       │   │   └── audit.html            # Access logs and statistics
│   │       └── db/changelog/                 # Liquibase database migrations (prod only)
│   ├── test/
│   │   └── java/com/example/service/
│   │       └── UserServiceTest.java          # Unit tests (49 tests total)
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
- ✅ Login page at `http://localhost:8080/login` (demo credentials required)

## User Authentication

The application includes a simple authentication system with login/logout functionality.

**Demo Credentials:**
- **Username:** admin
- **Password:** admin123

**Features:**
- Form-based login at `/login`
- Session-based authentication
- Protected pages (require login)
- Public API endpoints (no authentication required)
- Logout functionality

**Login Flow:**
1. Access `http://localhost:8080/`
2. Redirected to `/login` automatically
3. Enter credentials (admin / admin123)
4. Redirected to home page after successful login
5. Click logout button to end session

**Technical Details:**
- AuthUser entity stores authentication credentials
- Liquibase migration 004 creates auth_user table
- Spring Security configuration enforces authentication
- Jetty SessionManager handles session state
- **NOTE:** Passwords are NOT hashed (for demo). Implement BCrypt for production.

### Web Dashboard UI

A simple multi-page application is included to interact with all API endpoints:

**URL:** `http://localhost:8080/` (Home page with navigation)

**Architecture:**
- **Home Page** at `/` — Navigation dashboard with links to feature pages
- **Feature Pages** — Separate dedicated pages for each operation type
  - `/pages/users.html` — User management (create, read, update, delete)
  - `/pages/documents.html` — Document management (CRUD + full-text search)
  - `/pages/sharing.html` — Document sharing and access control
  - `/pages/categories.html` — Category management for documents
  - `/pages/audit.html` — Access logs and usage statistics

**Features:**
- **Clean & Simple:** One page per feature area, easy to navigate
- **Interactive Forms:** Tab-based interface for different operations
- **Live API Testing:** All endpoints tested interactively from the UI
- **Error Handling:** Proper error display with HTTP status codes
- **No External Dependencies:** Pure HTML/CSS/JavaScript (packaged in JAR)
- **Responsive Design:** Works on desktop and mobile browsers

**Profiles:**
- **dev:** Home page shows status info and system information (no API access)
- **prod:** Full interactive pages with all API operations enabled

**Endpoint Coverage (All 22+ endpoints):**

*Users (6 endpoints):*
- `GET /api/users` — List all users
- `GET /api/users/{id}` — Get user by ID
- `GET /api/users/search/username?username=...` — Search by username
- `POST /api/users` — Create new user
- `PUT /api/users/{id}` — Update user
- `DELETE /api/users/{id}` — Delete user

*Documents (8 endpoints):*
- `GET /api/documents/user/{ownerId}` — Get user's documents
- `GET /api/documents/{id}` — Get document by ID
- `GET /api/documents/search?title=...` — Search documents
- `POST /api/documents` — Create document
- `PUT /api/documents/{id}` — Update document
- `DELETE /api/documents/{id}` — Delete document (soft delete)
- `GET /api/documents/categories/{ownerId}` — List categories
- `POST /api/documents/categories` — Create category

*Sharing & Collaboration (4 endpoints):*
- `POST /api/documents/{id}/share` — Share document with user
- `DELETE /api/documents/{id}/share/{shareId}` — Revoke share
- `GET /api/documents/{id}/shares` — Get all shares for document
- `GET /api/documents/shared-with/{userId}` — Get documents shared with user
- `GET /api/documents/{id}/access` — Check if user has access

*Audit & Access Logs (3 endpoints):*
- `GET /api/documents/{id}/access-log` — Get full access log
- `GET /api/documents/{id}/access-log/recent?days=7` — Get recent logs
- `GET /api/documents/{id}/access-stats` — Get access statistics

**Quick Start with UI:**

```bash
# Build the application (UI is packaged in JAR)
mvn clean package

# Run with dev profile (status page only)
java -jar target/java8-spring-app-1.0.0.jar --spring.profiles.active=dev
# Visit: http://localhost:8080/

# Run with prod profile (full interactive dashboard)
java -jar target/java8-spring-app-1.0.0.jar --spring.profiles.active=prod
# Visit: http://localhost:8080/
# Create test users/documents and use the interactive UI
```

**Implementation Details:**
- **Served by:** `StatusController.java` (detects active profile and renders appropriate UI)
- **Packaging:** Static HTML served directly from controller (no additional build tools needed)
- **Size:** ~32KB embedded in JAR (~44MB total)
- **Technologies:** Vanilla JavaScript, HTML5, CSS3 (no external dependencies, works offline)
- **Error Handling:** User-friendly error messages for all API failures
- **Profile-Aware:** Automatically shows/hides features based on `--spring.profiles.active`

**Example Usage Flow:**

1. Start app in prod profile
2. Go to `http://localhost:8080/`
3. Use "User Management" tab to create a test user
4. Use "Documents" tab to create a document
5. Use "Sharing" tab to share the document with another user
6. Use "Audit Logs" tab to view access history
7. All changes reflect in database immediately

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

1. **Dashboard UI and API Endpoints**: The dashboard at `http://localhost:8080/` works in both dev and prod profiles:
   - **dev profile:** Shows status page only (UserController and DocumentController endpoints disabled)
   - **prod profile:** Shows full interactive dashboard with all API endpoints enabled
   - To use all dashboard features, run with `--spring.profiles.active=prod`

2. **Dev Profile Architecture**: 
   - No database dependencies (UserService and UserController disabled via @Profile("prod"))
   - JPA repositories NOT enabled (only in prod/test/integration-test profiles)
   - EntityManagerFactory NOT created
   - Allows clean development without PostgreSQL setup
   - Use prod profile for full application features

3. **UI Features by Endpoint**:
   - **User Management:** All CRUD operations (create, read, update, delete users)
   - **Document Management:** Full document lifecycle (create, read, update, delete, search)
   - **Document Sharing:** Share documents with other users, revoke access, check permissions
   - **Categories:** Create and list document categories
   - **Audit & Logs:** View access logs, recent activity (7-day window), and access statistics
   - All endpoints have try-catch error handling with user-friendly error messages

4. **Startup Time**: Application starts quickly (~3 seconds in dev, longer with database in prod)
