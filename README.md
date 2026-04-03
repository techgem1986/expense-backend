# Expense Management Service

A comprehensive expense management application built with Spring Boot, featuring recurring transactions, budget tracking, and analytics.

## Features

- **User Authentication**: JWT-based authentication with RSA keys
- **Transaction Management**: Track income and expenses with categories
- **Recurring Transactions**: Monthly recurring transactions with auto-generation
- **Budget Management**: Set budgets with alert thresholds
- **Analytics**: Dashboard with spending trends and category breakdowns
- **Event-Driven Architecture**: Domain events for loose coupling

## Technology Stack

- **Backend**: Spring Boot 3.x, Java 17
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Scheduler**: Quartz (for recurring transactions)
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI/Swagger

## Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven (or use Maven wrapper)

### Local Development Setup

1. **Clone and navigate to the project**
   ```bash
   cd expense-management-service
   ```

2. **Start infrastructure (PostgreSQL + Redis)**
   ```bash
   docker-compose up -d postgres redis
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

### Docker Setup

1. **Build and run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Transactions
- `GET /api/transactions` - List user transactions
- `POST /api/transactions` - Create transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction

### Recurring Transactions
- `GET /api/recurring-transactions` - List recurring transactions
- `POST /api/recurring-transactions` - Create recurring transaction
- `PUT /api/recurring-transactions/{id}?editMode=INDIVIDUAL|SERIES` - Update recurring transaction
- `DELETE /api/recurring-transactions/{id}` - Deactivate recurring transaction

### Budgets
- `GET /api/budgets` - List user budgets
- `POST /api/budgets` - Create budget
- `GET /api/budgets/{id}/status` - Get budget status

### Analytics
- `GET /api/analytics/dashboard` - Dashboard summary
- `GET /api/analytics/trends` - Spending trends
- `GET /api/analytics/categories` - Category breakdown

### Alerts
- `GET /api/alerts` - List user alerts

## Database Schema

The application uses the following main tables:
- `users` - User accounts
- `categories` - Transaction categories
- `transactions` - Individual transactions
- `recurring_transactions` - Recurring transaction templates
- `budgets` - Budget definitions
- `budget_categories` - Budget-category relationships
- `alerts` - System alerts and notifications

## Development

### Project Structure

```
src/main/java/com/expenseapp/
├── user/                 # User management module
├── transaction/          # Transaction management module
├── recurring/           # Recurring transactions module
├── budget/              # Budget management module
├── category/            # Category management module
├── alert/               # Alert system module
├── analytics/           # Analytics and reporting module
└── shared/              # Shared infrastructure
    ├── config/          # Configuration classes
    ├── security/        # Security utilities
    ├── dto/             # Shared DTOs
    └── exception/       # Global exception handling
```

### Testing

Run tests with:
```bash
./mvnw test
```

Generate test coverage report:
```bash
./mvnw jacoco:report
```

### Code Quality

- Follow domain-driven design principles
- Package by feature, not by layer
- Use constructor injection for dependencies
- Write comprehensive unit and integration tests

## Deployment

### Production Considerations

1. **Database**: Use connection pooling and proper indexing
2. **Redis**: Configure persistence and clustering for production
3. **Security**: Store JWT keys securely (AWS KMS, HashiCorp Vault)
4. **Monitoring**: Enable metrics and distributed tracing
5. **Quartz**: Consider database-backed job store for clustering

### Environment Variables

For production deployment, configure:

```bash
DATABASE_URL=jdbc:postgresql://prod-db:5432/expense_db
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=secure_password
REDIS_URL=redis://prod-redis:6379
JWT_PRIVATE_KEY_PATH=classpath:keys/prod-private.pem
JWT_PUBLIC_KEY_PATH=classpath:keys/prod-public.pem
```

## Contributing

1. Follow the established code structure
2. Write tests for new features
3. Update documentation as needed
4. Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License.