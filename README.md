# Loan Management System - Spring Boot Microservices (2019)

## Project Overview

Loan management system built with Spring Boot 2.2.6 for a credit union managing personal loans, auto loans, and mortgages. The system implements microservices architecture with Spring Cloud, handling loan origination, automated underwriting, approval workflows, disbursement, repayment tracking, and collections.

## Architecture

**Microservices Architecture with Spring Cloud**

```
                    Spring Cloud Gateway (Port 8080)
                              ↓
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼────────┐   ┌────────▼───────┐   ┌────────▼────────┐
│  Loan Service  │   │Customer Service│   │Payment Service  │
│  (Port 8081)   │   │  (Port 8082)   │   │  (Port 8083)    │
└───────┬────────┘   └────────┬───────┘   └────────┬────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              ↓
                    PostgreSQL Databases
                       (per service)
```

## Technology Stack

| Component        | Technology       | Version |
| ---------------- | ---------------- | ------- |
| Framework        | Spring Boot      | 2.2.6   |
| Language         | Java             | 11      |
| Build Tool       | Maven            | 3.6     |
| Database         | PostgreSQL       | 11      |
| ORM              | Spring Data JPA  | 2.2     |
| Security         | Spring Security  | 5.2     |
| Cache            | Redis            | 5.0     |
| Messaging        | RabbitMQ         | 3.8     |
| API Docs         | Swagger/OpenAPI  | 3.0     |
| Testing          | JUnit 5, Mockito | -       |
| Containerization | Docker           | 19.03   |
| Orchestration    | Kubernetes       | 1.17    |

## Project Structure

```
loan-system/
├── api-gateway/                    # Spring Cloud Gateway
│   ├── src/main/java/
│   │   └── com/creditunion/gateway/
│   │       ├── GatewayApplication.java
│   │       ├── config/
│   │       └── filters/
│   └── pom.xml
│
├── loan-service/                   # Loan Management Microservice
│   ├── src/main/java/
│   │   └── com/creditunion/loan/
│   │       ├── LoanServiceApplication.java
│   │       ├── entity/
│   │       │   ├── Loan.java
│   │       │   ├── LoanApplication.java
│   │       │   └── LoanDocument.java
│   │       ├── repository/
│   │       │   ├── LoanRepository.java
│   │       │   └── LoanApplicationRepository.java
│   │       ├── service/
│   │       │   ├── LoanService.java
│   │       │   └── UnderwritingService.java
│   │       ├── controller/
│   │       │   └── LoanController.java
│   │       ├── dto/
│   │       │   ├── LoanDTO.java
│   │       │   └── LoanApplicationDTO.java
│   │       └── config/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── application-prod.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── customer-service/               # Customer Management Microservice
│   ├── src/main/java/
│   │   └── com/creditunion/customer/
│   │       ├── CustomerServiceApplication.java
│   │       ├── entity/
│   │       │   ├── Customer.java
│   │       │   └── CreditScore.java
│   │       ├── repository/
│   │       ├── service/
│   │       └── controller/
│   ├── Dockerfile
│   └── pom.xml
│
├── payment-service/                # Payment Processing Microservice
│   ├── src/main/java/
│   │   └── com/creditunion/payment/
│   │       ├── PaymentServiceApplication.java
│   │       ├── entity/
│   │       │   ├── Payment.java
│   │       │   └── PaymentSchedule.java
│   │       ├── repository/
│   │       ├── service/
│   │       └── controller/
│   ├── Dockerfile
│   └── pom.xml
│
├── kubernetes/                     # Kubernetes Configurations
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   └── secrets.yaml
│
└── docker-compose.yml             # Local Development Setup
```

## Core Features

### 1. Loan Origination

- Online loan application with document upload
- Multiple loan types (Personal, Auto, Mortgage)
- Application status tracking
- Document verification

### 2. Automated Underwriting

- Credit scoring integration (FICO, Experian, TransUnion)
- Automated decision engine
- Risk assessment algorithms
- Income verification

### 3. Approval Workflow

- Multi-level approval workflow
- Role-based approvals
- Approval history tracking
- Email notifications

### 4. Loan Disbursement

- Disbursement scheduling
- Account setup automation
- Bank integration
- Disbursement tracking

### 5. Repayment Management

- Amortization schedule generation
- Payment processing
- Payment allocation logic
- Early payment handling

### 6. Collections Management

- Delinquency tracking
- Automated reminders
- Collection workflows
- Late payment fees

## Database Schema

### Loan Service Database

**loans** - Loan records

- loan_id (PK, BIGSERIAL)
- customer_id (FK)
- loan_type (VARCHAR) - PERSONAL, AUTO, MORTGAGE
- loan_amount (DECIMAL)
- interest_rate (DECIMAL)
- term_months (INTEGER)
- monthly_payment (DECIMAL)
- outstanding_balance (DECIMAL)
- status (VARCHAR)
- application_date (DATE)
- approval_date (DATE)
- disbursement_date (DATE)
- created_at, updated_at (TIMESTAMP)

**loan_applications** - Loan applications

- application_id (PK)
- customer_id (FK)
- loan_type, requested_amount
- employment_info (JSONB)
- income_verification (JSONB)
- status, created_at, updated_at

**loan_documents** - Supporting documents

- document_id (PK)
- loan_id (FK)
- document_type, document_url
- verified, uploaded_at

### Customer Service Database

**customers** - Customer information

- customer_id (PK)
- first_name, last_name, email, phone
- ssn_encrypted (VARCHAR)
- date_of_birth, address (JSONB)
- created_at, updated_at

**credit_scores** - Credit history

- score_id (PK)
- customer_id (FK)
- credit_bureau (VARCHAR)
- score (INTEGER)
- score_date (DATE)

### Payment Service Database

**payments** - Payment transactions

- payment_id (PK)
- loan_id (FK)
- payment_amount (DECIMAL)
- principal_amount, interest_amount
- payment_date, payment_method
- status, transaction_id

**payment_schedules** - Amortization schedules

- schedule_id (PK)
- loan_id (FK)
- payment_number, due_date
- scheduled_amount, principal, interest
- paid_amount, payment_status

## API Endpoints

### Loan Service APIs

```
POST   /api/loans                      # Create loan application
GET    /api/loans/{id}                 # Get loan details
PUT    /api/loans/{id}/approve         # Approve loan
POST   /api/loans/{id}/disburse        # Disburse loan
GET    /api/loans/customer/{customerId} # Get customer loans
PUT    /api/loans/{id}/status          # Update loan status
```

### Customer Service APIs

```
POST   /api/customers                  # Create customer
GET    /api/customers/{id}             # Get customer details
PUT    /api/customers/{id}             # Update customer
GET    /api/customers/{id}/credit-score # Get credit score
```

### Payment Service APIs

```
POST   /api/payments                   # Record payment
GET    /api/payments/loan/{loanId}     # Get loan payments
GET    /api/payments/schedule/{loanId} # Get payment schedule
POST   /api/payments/schedule          # Generate schedule
```

### Underwriting APIs

```
POST   /api/underwriting/evaluate      # Evaluate loan application
GET    /api/underwriting/rules         # Get underwriting rules
POST   /api/credit/score/{customerId}  # Get credit score from bureau
```

## Configuration

### application.yml (Loan Service)

```yaml
server:
  port: 8081

spring:
  application:
    name: loan-service
  datasource:
    url: jdbc:postgresql://localhost:5432/loan_db
    username: loan_user
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# Swagger Configuration
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# Custom Configuration
loan:
  underwriting:
    min-credit-score: 650
    max-debt-to-income-ratio: 0.43
    min-employment-months: 24
  interest-rates:
    personal-loan:
      excellent: 5.99
      good: 8.99
      fair: 12.99
    auto-loan:
      excellent: 3.99
      good: 6.99
      fair: 9.99
    mortgage:
      excellent: 3.25
      good: 4.25
      fair: 5.50
```

## Docker Configuration

### Dockerfile (Loan Service)

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/loan-service-1.0.0.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### docker-compose.yml

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:11
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
      POSTGRES_MULTIPLE_DATABASES: loan_db,customer_db,payment_db
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:5-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - loan-service
      - customer-service
      - payment-service

  loan-service:
    build: ./loan-service
    ports:
      - "8081:8081"
    environment:
      DB_PASSWORD: admin123
      SPRING_PROFILES_ACTIVE: dev
    depends_on:
      - postgres
      - redis
      - rabbitmq

  customer-service:
    build: ./customer-service
    ports:
      - "8082:8082"
    depends_on:
      - postgres

  payment-service:
    build: ./payment-service
    ports:
      - "8083:8083"
    depends_on:
      - postgres

volumes:
  postgres_data:
```

## Kubernetes Deployment

### deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: loan-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: loan-service
  template:
    metadata:
      labels:
        app: loan-service
    spec:
      containers:
        - name: loan-service
          image: creditunion/loan-service:1.0.0
          ports:
            - containerPort: 8081
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secrets
                  key: password
          resources:
            requests:
              memory: "512Mi"
              cpu: "500m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 20
            periodSeconds: 5
```

## CI/CD Pipeline (GitLab CI)

### .gitlab-ci.yml

```yaml
stages:
  - build
  - test
  - package
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

build:
  stage: build
  image: maven:3.6-jdk-11
  script:
    - mvn clean compile
  cache:
    paths:
      - .m2/repository

test:
  stage: test
  image: maven:3.6-jdk-11
  script:
    - mvn test
    - mvn verify
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/site/jacoco

package:
  stage: package
  image: maven:3.6-jdk-11
  script:
    - mvn package -DskipTests
    - docker build -t creditunion/loan-service:$CI_COMMIT_SHA .
    - docker push creditunion/loan-service:$CI_COMMIT_SHA
  only:
    - main
    - develop

deploy-staging:
  stage: deploy
  image: kubectl:latest
  script:
    - kubectl set image deployment/loan-service loan-service=creditunion/loan-service:$CI_COMMIT_SHA -n staging
    - kubectl rollout status deployment/loan-service -n staging
  environment:
    name: staging
  only:
    - develop

deploy-production:
  stage: deploy
  image: kubectl:latest
  script:
    - kubectl set image deployment/loan-service loan-service=creditunion/loan-service:$CI_COMMIT_SHA -n production
    - kubectl rollout status deployment/loan-service -n production
  environment:
    name: production
  when: manual
  only:
    - main
```

## Testing

### Unit Tests (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UnderwritingService underwritingService;

    @InjectMocks
    private LoanService loanService;

    @Test
    void testCreateLoanApplication() {
        // Arrange
        LoanApplication application = new LoanApplication();
        application.setCustomerId(1L);
        application.setRequestedAmount(new BigDecimal("50000"));

        // Act & Assert
        assertDoesNotThrow(() -> loanService.createApplication(application));
    }
}
```

### Integration Tests (TestContainers)

```java
@SpringBootTest
@Testcontainers
class LoanServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:11");

    @Autowired
    private LoanService loanService;

    @Test
    void testLoanCreationEndToEnd() {
        // Integration test with real database
    }
}
```

## Security

### Authentication & Authorization

- OAuth 2.0 with JWT tokens
- Role-based access control (RBAC)
- API Gateway authentication
- Service-to-service authentication

### Data Security

- PII encryption at rest
- TLS/SSL for data in transit
- Database connection encryption
- Secrets management (Kubernetes Secrets/Vault)

### Compliance

- PCI-DSS for payment data
- SOC 2 Type II compliance
- GLBA (Gramm-Leach-Bliley Act)
- FCRA (Fair Credit Reporting Act)

## Monitoring & Logging

### Application Monitoring

- Spring Boot Actuator endpoints
- Prometheus metrics collection
- Grafana dashboards
- Distributed tracing with Sleuth + Zipkin

### Logging

- Structured logging (JSON format)
- Centralized logging with ELK Stack
- Log levels per environment
- Correlation IDs for request tracking

## Performance Optimization

### Caching Strategy

- Redis for frequently accessed data
- Spring Cache abstraction
- Cache eviction policies
- Cache warming strategies

### Database Optimization

- Indexed columns for common queries
- Connection pooling (HikariCP)
- Read replicas for reporting
- Database partitioning for large tables

### Scalability

- Horizontal pod autoscaling (HPA)
- Load balancing with Kubernetes
- Async processing with RabbitMQ
- Rate limiting at API Gateway

## Installation & Setup

### Prerequisites

- Java 11+
- Maven 3.6+
- Docker 19.03+
- Kubernetes 1.17+ (for production)
- PostgreSQL 11+

### Local Development Setup

```bash
# Clone repository
git clone https://github.com/creditunion/loan-system.git
cd loan-system

# Start infrastructure services
docker-compose up -d postgres redis rabbitmq

# Build all services
mvn clean install

# Run loan service
cd loan-service
mvn spring-boot:run

# Access Swagger UI
open http://localhost:8081/swagger-ui.html
```

### Production Deployment

```bash
# Build Docker images
mvn clean package
docker build -t creditunion/loan-service:1.0.0 loan-service/

# Deploy to Kubernetes
kubectl apply -f kubernetes/
kubectl rollout status deployment/loan-service
```

## Future Enhancements

- AI/ML for credit risk assessment
- Blockchain for loan documentation
- Mobile app for borrowers
- Open Banking API integration
- Real-time fraud detection
- Chatbot for customer queries
- Predictive analytics for collections

## Team

- **Architects**: 2
- **Backend Developers**: 6 (Java/Spring Boot)
- **DevOps Engineers**: 2
- **QA Engineers**: 3
- **Product Owner**: Credit Union Business Analyst

## License

Proprietary - Credit Union Internal Use Only

---

**Last Updated**: November 2025  
**Version**: 1.0.0  
**Status**: Production - Microservices Architecture
