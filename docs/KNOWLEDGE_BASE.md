# Expense Backend - Knowledge Base

> **Last Updated:** 2024-05-20
>
> Backend-specific knowledge for the Expense Management System.

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Programming Language |
| Spring Boot | 4.1.2 | Application Framework |
| Spring Security | 7.1 | Security Framework |
| Spring Data JPA | 4.x | ORM/Data Access |
| PostgreSQL | 15 | Primary Database |
| MapStruct | 1.6.3 | DTO Mapping |
| OpenAPI/Swagger | 3.2.0 | API Documentation |
| JWT (Nimbus JOSE) | — | Token Authentication |
| Lombok | — | Code Generation |
| Flyway | 12.4.0 | Database Migrations (auto-run on startup) |
| Apache POI | — | Excel Export |
| Mockito | — | Unit Testing |
| Testcontainers | — | Integration Testing |

---

## Project Structure

```
expense-backend/
├── src/
│   ├── main/
│   │   ├── java/com/expenseapp/
│   │   │   ├── Application.java           # Main entry point
│   │   │   ├── account/                   # Account management module
│   │   │   │   ├── api/                  # AccountController
│   │   │   │   ├── domain/               # Account entity
│   │   │   │   ├── dto/                  # Account DTOs
│   │   │   │   ├── mapper/               # AccountMapper
│   │   │   │   ├── repository/           # AccountRepository
│   │   │   │   └── service/              # AccountService
│   │   │   ├── alert/                    # Alert system module
│   │   │   │   ├── api/                  # AlertController
│   │   │   │   ├── config/              # Alert configuration
│   │   │   │   ├── domain/              # Alert entity
│   │   │   │   ├── dto/                 # Alert DTOs
│   │   │   │   ├── repository/          # AlertRepository
│   │   │   │   └── service/             # AlertService
│   │   │   ├── analytics/               # Analytics module
│   │   │   │   ├── api/                 # AnalyticsController
│   │   │   │   ├── dto/                 # Analytics DTOs
│   │   │   │   └── service/            # AnalyticsService
│   │   │   ├── budget/                  # Budget management module
│   │   │   │   ├── api/                 # BudgetController
│   │   │   │   ├── config/             # Budget configuration
│   │   │   │   ├── domain/             # Budget, BudgetCategory entities
│   │   │   │   ├── dto/                # Budget DTOs
│   │   │   │   ├── event/              # BudgetThresholdExceededEvent
│   │   │   │   ├── mapper/             # BudgetMapper
│   │   │   │   ├── repository/         # BudgetRepository
│   │   │   │   └── service/            # BudgetService
│   │   │   ├── category/               # Category management module
│   │   │   │   ├── api/                # CategoryController
│   │   │   │   ├── config/             # Category configuration
│   │   │   │   ├── domain/             # Category entity
│   │   │   │   ├── dto/                # Category DTOs
│   │   │   │   ├── mapper/             # CategoryMapper
│   │   │   │   ├── repository/         # CategoryRepository
│   │   │   │   └── service/            # CategoryService
│   │   │   ├── recurring/              # Recurring transactions module
│   │   │   │   ├── api/                # RecurringTransactionController
│   │   │   │   ├── config/             # Recurring configuration
│   │   │   │   ├── domain/             # RecurringTransaction entity
│   │   │   │   ├── dto/                # Recurring DTOs
│   │   │   │   ├── event/              # RecurringTransactionGeneratedEvent
│   │   │   │   ├── job/                # RecurringTransactionJob (@Scheduled)
│   │   │   │   ├── mapper/             # RecurringTransactionMapper
│   │   │   │   ├── repository/         # RecurringTransactionRepository
│   │   │   │   └── service/            # RecurringTransactionService
│   │   │   ├── search/                 # Global search module
│   │   │   ├── transaction/            # Transaction management module
│   │   │   ├── user/                   # User management module
│   │   │   │   ├── api/                # AuthController, UserController
│   │   │   │   ├── domain/             # User entity
│   │   │   │   ├── dto/                # User DTOs
│   │   │   │   ├── mapper/             # UserMapper
│   │   │   │   ├── repository/         # UserRepository
│   │   │   │   └── service/            # UserService
│   │   │   └── shared/                 # Shared infrastructure
│   │   │       ├── config/
│   │   │       │   ├── CachingConfiguration.java
│   │   │       │   ├── JpaAuditingConfiguration.java
│   │   │       │   ├── OpenApiConfiguration.java
│   │   │       │   └── SecurityConfiguration.java
│   │   │       ├── dto/
│   │   │       │   └── ApiResponse.java
│   │   │       ├── entity/
│   │   │       │   └── BaseEntity.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   └── ValidationException.java
│   │   │       └── security/
│   │   │           ├── JwtService.java
│   │   │           └── RateLimitingFilter.java
│   │   └── resources/
│   │       ├── application.yaml           # Main configuration
│   │       ├── application-dev.yaml       # Dev profile
│   │       ├── application-prod.yaml      # Production profile
│   │       └── db/migration/
│   │           └── V1__initial_schema.sql         # Tables + indexes + grants
│   └── test/
│       ├── java/com/expenseapp/           # Mirror of main structure
│       │   ├── account/
│       │   ├── alert/
│       │   ├── analytics/
│       │   ├── budget/
│       │   ├── category/
│       │   ├── recurring/
│       │   ├── search/
│       │   ├── service/
│       │   ├── shared/
│       │   ├── transaction/
│       │   ├── user/
│       │   └── ApplicationStructureTest.java
│       └── resources/
│           └── application-test.yaml
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
├── .gitignore
└── logs/
```

---

## Module Architecture (Package by Feature)

Each feature module follows a consistent structure:

```
module/
├── api/           # REST Controllers (app/web layer)
├── domain/        # JPA Entities (domain models)
├── dto/           # Data Transfer Objects (request/response)
├── mapper/        # MapStruct mappers (entity ↔ DTO)
├── repository/    # Spring Data JPA repositories
├── service/       # Business logic services
├── config/        # Module-specific configuration (optional)
└── event/         # Domain events (optional)
```

---

## Database Schema

### 1. `users`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| is_active | BOOLEAN | DEFAULT true |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 2. `categories`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| name | VARCHAR(100) | UNIQUE, NOT NULL |
| description | TEXT | |
| type | VARCHAR(50) | INCOME/EXPENSE |
| user_id | BIGINT | FK → users(id) |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 3. `transactions`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK → users(id) |
| category_id | BIGINT | FK → categories(id) |
| amount | DECIMAL(15,2) | NOT NULL |
| type | VARCHAR(50) | INCOME/EXPENSE |
| description | TEXT | |
| transaction_date | DATE | NOT NULL |
| is_recurring_instance | BOOLEAN | DEFAULT false |
| linked_recurring_transaction_id | BIGINT | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 4. `recurring_transactions`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK → users(id) |
| category_id | BIGINT | FK → categories(id) |
| name | VARCHAR(255) | NOT NULL |
| amount | DECIMAL(15,2) | NOT NULL |
| type | VARCHAR(50) | INCOME/EXPENSE |
| description | TEXT | |
| frequency | VARCHAR(50) | MONTHLY |
| day_of_month | INTEGER | 1-31 |
| start_date | DATE | |
| end_date | DATE | |
| next_execution_date | DATE | |
| is_active | BOOLEAN | DEFAULT true |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 5. `budgets`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK → users(id) |
| name | VARCHAR(255) | NOT NULL |
| limit_amount | DECIMAL(15,2) | NOT NULL |
| period | VARCHAR(50) | MONTHLY/YEARLY |
| alert_threshold | DECIMAL(5,2) | DEFAULT 0.80 |
| start_date | DATE | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 6. `budget_categories`
| Column | Type | Constraints |
|--------|------|-------------|
| budget_id | BIGINT | PK, FK → budgets(id) |
| category_id | BIGINT | PK, FK → categories(id) |

### 7. `alerts`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK → users(id) |
| type | VARCHAR(100) | NOT NULL |
| message | TEXT | NOT NULL |
| related_entity_type | VARCHAR(100) | |
| related_entity_id | BIGINT | |
| is_read | BOOLEAN | DEFAULT false |
| read_at | TIMESTAMP | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 8. `accounts`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK → users(id) |
| name | VARCHAR(255) | NOT NULL |
| type | VARCHAR(100) | SAVINGS/CREDIT_CARD/CHECKING/LOAN/etc. |
| current_balance | DECIMAL(15,2) | NOT NULL (no `@PositiveOrZero`) |
| currency | VARCHAR(10) | |
| is_active | BOOLEAN | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

---

## Flyway Migrations

| Migration | Location | Description |
|-----------|----------|-------------|
| V1__initial_schema.sql | db/migration | Initial schema: users, categories, accounts, transactions, recurring_transactions, budgets, budget_categories, alerts + all indexes + grants |

Flyway runs automatically on application startup:
- all environments: `classpath:db/migration`
- pre-existing databases are adopted with `spring.flyway.baseline-on-migrate: true` (baseline version 0)
- Hibernate `ddl-auto` is `none` - Flyway is the single owner of the schema
- no seed/demo data is shipped; the migrations create the schema only

---

## Security Configuration

| Setting | Value |
|---------|-------|
| JWT Algorithm | HS256 (Nimbus JOSE) |
| Token Expiration | 24 hours (86400 seconds) |
| Password Encoding | BCrypt |
| CORS Origins | localhost:3000 (configurable) |
| Session Policy | Stateless (no server sessions) |
| Rate Limiting | Basic filter |

### Security Chain (Spring Security)
```
Authentication → JWT Token Filter → Request Authorization → Controller
```

---

## API Endpoints

### Authentication
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| POST | `/api/auth/register` | No |
| POST | `/api/auth/login` | No |

### Users
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/users/profile` | Yes |
| GET | `/api/users/{userId}` | Yes |

### Categories
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/categories` | Yes |
| GET | `/api/categories/type/{type}` | Yes |
| POST | `/api/categories` | Yes |
| PUT | `/api/categories/{id}` | Yes |
| DELETE | `/api/categories/{id}` | Yes |

### Transactions
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/transactions?page=&size=&sort=` | Yes |
| GET | `/api/transactions/{id}` | Yes |
| POST | `/api/transactions` | Yes |
| PUT | `/api/transactions/{id}` | Yes |
| DELETE | `/api/transactions/{id}` | Yes |

### Recurring Transactions
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/recurring-transactions` | Yes |
| GET | `/api/recurring-transactions/{id}` | Yes |
| POST | `/api/recurring-transactions` | Yes |
| PUT | `/api/recurring-transactions/{id}` | Yes |
| DELETE | `/api/recurring-transactions/{id}` | Yes |

### Budgets
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/budgets?page=&size=&sort=` | Yes |
| GET | `/api/budgets/{id}` | Yes |
| POST | `/api/budgets` | Yes |
| PUT | `/api/budgets/{id}` | Yes |
| DELETE | `/api/budgets/{id}` | Yes |
| GET | `/api/budgets/check-threshold/{categoryId}` | Yes |

### Alerts
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/alerts?page=&size=&sort=` | Yes |
| GET | `/api/alerts/unread` | Yes |
| GET | `/api/alerts/unread/count` | Yes |
| PUT | `/api/alerts/{id}/read` | Yes |
| PUT | `/api/alerts/read-all` | Yes |
| DELETE | `/api/alerts/{id}` | Yes |

### Analytics
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/analytics` | Yes |
| GET | `/api/analytics/spending-by-category` | Yes |
| GET | `/api/analytics/current-month` | Yes |
| GET | `/api/analytics/year-to-date` | Yes |
| GET | `/api/export/comprehensive/report/excel` | Yes |

### Search
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/api/search?query=&type=&page=&size=` | Yes |
| GET | `/api/search/quick?query=` | Yes |

---

## API Response Format

**Success:**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* response payload */ }
}
```

**Error:**
```json
{
  "success": false,
  "message": "Error description",
  "errors": [ /* validation errors */ ]
}
```

---

## Event-Driven Architecture

| Event | Publisher | Consumer | Purpose |
|-------|-----------|----------|---------|
| TransactionCreatedEvent | TransactionService | BudgetService, AnalyticsService | Check budget thresholds, update analytics |
| BudgetThresholdExceededEvent | BudgetService | AlertService | Create budget alerts |
| RecurringTransactionGeneratedEvent | RecurringTransactionJob | BudgetService, AlertService | Check budgets after recurring generation |

---

## Key Configurations

### application.yaml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: update

app:
  jwt:
    secret: "your-secret-key-for-development-only"
    expiration: 86400
  cors:
    allowed-origins: "http://localhost:3000,http://localhost:8080"
```

### Production Optimizations
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

---

## Bugs & Fixes History

### 1. Account Balance Validation Fix
- **Issue**: `@PositiveOrZero` on `currentBalance` prevented transactions for liability accounts
- **Fix**: Removed `@PositiveOrZero` constraint, kept `@NotNull`
- **Affected File**: `account/domain/Account.java`

### 2. Transaction 400 Error (Double vs BigDecimal)
- **Issue**: Backend receives numeric Double instead of BigDecimal for amount field
- **Fix**: Frontend converts amount to string before POST (frontend-side fix)

### 3. Validation Error Handling Enhancement
- **Backend**: Global exception handler `GlobalExceptionHandler.java` returns proper HTTP status codes and messages
- **Validation**: Bean Validation annotations at API boundary with meaningful error messages

---

## Development Commands

```bash
# Start infrastructure (PostgreSQL)
docker-compose up -d postgres

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package -DskipTests

# Generate coverage report
./mvnw jacoco:report
```

### Docker
```dockerfile
FROM maven:3.9-eclipse-temurin-25 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre
COPY --from=build target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## Coding Conventions

### Principles
- **Clean Code**: Readable, maintainable, SOLID, DRY, KISS
- **Package by Feature**: Each module contains all related classes
- **Constructor Injection**: `@RequiredArgsConstructor` with `final` dependencies
- **No Field Injection**: Prefer constructor injection over `@Autowired` on fields

### Naming
| Type | Convention | Example |
|------|-----------|---------|
| Classes | PascalCase | `UserService` |
| Methods | camelCase | `createUser()` |
| Variables | camelCase | `userName` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Interfaces | Descriptive | `UserRepository` |
| Tests | should_..._when_... | `shouldCreateUser_WhenValidRequest()` |

### Test Pattern (AAA)
```java
@Test
void shouldCreateUser_WhenValidRequest() {
    // Arrange
    // Act
    // Assert
    verify(repository, times(1)).save(any());
}
```

---

## Configuration Profiles

| Profile | Purpose |
|---------|---------|
| `dev` (default) | Local development with dev settings |
| `prod` | Production environment |
| `test` | Testing environment |

---

## Environment Variables (Production)

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Profile: `prod` |
| `DATABASE_URL` | JDBC connection string |
| `DATABASE_USERNAME` | DB username |
| `DATABASE_PASSWORD` | DB password |
| `APP_JWT_SECRET` | JWT signing secret |
| `APP_CORS_ALLOWED_ORIGINS` | Allowed CORS origins |
| `JAVA_OPTS` | JVM options (e.g., `-Xmx256m -Xms128m`) |