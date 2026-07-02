# SwiftPay - Payment Ledger

## Overview

SwiftPay is an event-driven peer-to-peer payment processing system built using Spring Boot, Apache Kafka, Redis, and PostgreSQL.

The system consists of two microservices:

1. **Transaction Gateway Service**
   - Accepts payment requests.
   - Performs request validations.
   - Ensures idempotency using Redis.
   - Stores transactions with PENDING status.
   - Publishes payment events to Kafka.
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/9da2c3d7-d2eb-45ea-85ca-b1108bc428f0" />

2. **Ledger Service**
   - Consumes payment events from Kafka.
   - Validates business rules.
   - Processes debit and credit operations.
   - Updates transaction status.
   - Publishes payment completion/failure events.

---

# Architecture

```text
+-------------------+
|      Client       |
+-------------------+
          |
          v
+----------------------------+
| Transaction Gateway Service |
+----------------------------+
          |
          | Save PENDING Transaction
          |
          +------------+
          |            |
          v            v
      Redis         Kafka
  (Idempotency)      |
                     |
                     v
      +----------------------+
      |   Ledger Service     |
      +----------------------+
                     |
                     v
              PostgreSQL
```

---

# Features

- Event-driven architecture using Kafka
- Payment initiation API
- Transaction status API
- Transaction history API
- Redis-based idempotency
- Sender/Receiver validation
- Balance validation
- Global exception handling
- Kafka retry mechanism
- Swagger/OpenAPI documentation
- Health check endpoints
- Dockerized deployment
- CI/CD using GitHub Actions

---

# Technology Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.x |
| Spring Data JPA | Latest |
| PostgreSQL | 16 |
| Apache Kafka | 7.5 |
| Redis | 7 |
| Docker | Latest |
| Docker Compose | Latest |
| Swagger/OpenAPI | Latest |
| GitHub Actions | Latest |

---

# Project Structure

```text
swiftpay/
│
├── swiftpay-gateway/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── swiftpay-ledger/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── docker-compose.yml
├── README.md
└── .github/workflows/build.yml
```

---

# Kafka Topics

| Topic Name | Description |
|------------|-------------|
| payment-initiated | Published by Gateway Service |
| payment-completed | Published by Ledger Service |
| payment-failed | Published by Ledger Service |

---

# APIs

## Transaction Gateway Service

### Create Payment

**POST**

```http
/v1/payments
```

Sample Request:

```json
{
  "transactionId": "TXN1001",
  "senderId": 1,
  "receiverId": 2,
  "amount": 500,
  "currency": "INR"
}
```

Sample Response:

```json
{
  "transactionId": "TXN1001",
  "status": "PENDING",
  "message": "Payment accepted for processing"
}
```

---

### Get Transaction Status

**GET**

```http
/v1/payments/{transactionId}
```

Example:

```http
/v1/payments/TXN1001
```

Sample Response:

```json
{
  "transactionId": "TXN1001",
  "status": "SUCCESS",
  "message": "Transaction status fetched successfully"
}
```

---

## Ledger Service

### Create User

**POST**

```http
/v1/ledger/users
```

Sample Request:

```json
{
  "id": 1,
  "name": "Bharadwaj",
  "balance": 10000
}
```

---

### Get Transaction History

**GET**

```http
/v1/ledger/transactions/{userId}
```

Example:

```http
/v1/ledger/transactions/1
```

Sample Response:

```json
[
  {
    "transactionId": "TXN1001",
    "senderId": 1,
    "receiverId": 2,
    "amount": 500,
    "currency": "INR",
    "status": "SUCCESS"
  }
]
```

---

# Validation Rules

### Gateway Service

- Duplicate transactions are rejected.
- Sender and Receiver cannot be the same.
- Unsupported currencies are rejected.
- Sender account must exist.
- Receiver account must exist.

### Ledger Service

- Transaction must exist.
- Sender account must exist.
- Receiver account must exist.
- Sender must have sufficient balance.
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/06d8ba71-1282-4dce-b684-3601597ed425" />

---

# Idempotency

Redis is used to prevent duplicate transaction processing.

Duplicate requests with the same transaction ID are rejected.

---
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/7d142ae8-b1a6-4c66-a520-dfd83355fe36" />

# Running the Application

## Prerequisites

- Docker
- Docker Compose

---

## Clone Repository

```bash
git clone <repository-url>
cd swiftpay
```

---

## Start Entire Ecosystem

```bash
docker compose up -d
```

---

## Verify Running Containers

```bash
docker ps
```

Expected containers:

```text
postgres
redis
zookeeper
kafka
gateway
ledger
```

---

## Stop Application

```bash
docker compose down
```

---

# Swagger Documentation

## Gateway Service

```text
http://localhost:8080/swagger-ui/index.html
```
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/761cc3d7-1dd2-42b5-9638-06c32869da38" />


## Ledger Service

```text
http://localhost:8081/swagger-ui/index.html
```
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/e063d44b-336f-411c-8a00-db6b8ca56633" />

---

# Health Endpoints

## Gateway
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/fd1501bd-aa8d-4c4e-89d3-66147e3d8afb" />

```text
http://localhost:8080/actuator/health
```

## Ledger
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/32423565-4238-4a70-a833-247be2b9f062" />

```text
http://localhost:8081/actuator/health
```

---

# Logging

Application logs are enabled using SLF4J and Logback.

Important events logged:

- Payment processing started
- Payment completed
- Payment failed
- Validation failures
- Kafka event consumption

---

# Error Handling

Global exception handlers are implemented in both services.

Common errors:

- Duplicate Transaction
- Invalid Payment Request
- Resource Not Found
- Insufficient Funds
- Validation Failure

---

# Retry Mechanism

Kafka consumers are configured with retry support to handle temporary failures such as database unavailability.

---

# CI/CD

GitHub Actions workflow automatically:

- Builds Gateway Service
- Builds Ledger Service
- Runs Maven tests
- Builds Docker images

Workflow file:

```text
.github/workflows/build.yml
```

---

# Future Enhancements

- Notification Service
- Fraud Detection Service
- Multi-broker Kafka Cluster
- Prometheus & Grafana Monitoring
- Distributed Tracing
- Kubernetes Deployment

---
## Performance Testing

Performance testing was performed using Apache JMeter.

### Test Configuration

- Concurrent Users: 250
- Ramp-Up Period: 1 second
- Loop Count: 100
- Total Requests Executed: 25,000

### Results

| Metric | Value |
|---------|-------|
| Total Requests | 25,000 |
| Average Response Time | 522 ms |
| Throughput | 388.98 requests/sec |
| Received Throughput | 256.13 KB/sec |
| Sent Throughput | 103.77 KB/sec |

The `evidences/jmeter-metrics/Payment API.jmx` file contains the Apache JMeter test plan used for performance testing and can be imported directly into JMeter to reproduce the load test.

### Observations

- The system successfully processed approximately **389 transactions per second (TPS)**.
- The achieved throughput exceeded the required **250 TPS** target.
- No major failures were observed during the test execution.

### Identified Bottleneck

During load testing, database write operations were identified as the primary bottleneck because every payment request performs synchronous persistence before asynchronous processing through Kafka.

### Possible Optimizations

- Increase database connection pool size (HikariCP tuning).
- Add indexes on frequently queried columns such as `transaction_id`.
- Scale Gateway and Ledger services horizontally.
- Introduce Kafka producer batching.
- Deploy PostgreSQL on a dedicated instance for improved performance.

# Submission Criteria Mapping

The following table demonstrates how the solution satisfies the hackathon submission requirements.

| Submission Criteria | Implementation |
|---------------------|----------------|
| **Code Quality** | Clean layered architecture with Controller, Service, Repository, DTO, Entity, Exception, Event, and Utility layers. Meaningful naming conventions and separation of concerns are followed. |
| **Functionality** | End-to-end payment flow is implemented successfully. Payment requests are accepted, processed asynchronously through Kafka, and transaction status is updated accordingly. |
| **Insufficient Funds Handling** | Ledger Service validates sender balance and marks transactions as `FAILED` when funds are insufficient. |
| **DevOps Readiness** | Entire ecosystem (Gateway, Ledger, PostgreSQL, Kafka, Redis, Zookeeper) can be started using a single `docker compose up -d` command. |
| **Error Handling** | Global exception handling is implemented in both services using `@RestControllerAdvice`. Standard error responses and HTTP status codes are returned. |
| **Kafka Outage Handling** | Application logs and fails gracefully if Kafka becomes unavailable. |
| **Database Outage Handling** | Kafka consumer retry mechanism is implemented using Spring Kafka `@RetryableTopic` with configurable retry attempts and backoff intervals. |
| **Resilience** | Automatic retry mechanism for Kafka consumers ensures temporary database failures do not lead to message loss. |
| **Observability** | Health check endpoints are exposed through Spring Boot Actuator. Structured application logs are implemented using SLF4J and Logback. |
| **API Documentation** | All REST endpoints are documented using Swagger/OpenAPI. |
| **Caching & Idempotency** | Redis is used to ensure duplicate transactions are not processed within a 24-hour window. |
| **Event-Driven Architecture** | Apache Kafka is used for asynchronous communication between Gateway and Ledger services. |
| **Containerization** | Each microservice contains an individual Dockerfile and the complete ecosystem is orchestrated through Docker Compose. |
| **CI/CD** | GitHub Actions workflow compiles code, executes tests, and builds Docker images automatically. |
| **Performance Testing** | Load testing was performed using Apache JMeter to evaluate system behavior under concurrent requests. |
| **GitHub Repository** | Complete source code, documentation, Docker configuration, and CI/CD workflow are available in this repository. |

---

# Additional Notes

- Redis is used for idempotency and duplicate transaction prevention.
- PostgreSQL guarantees transactional consistency for debit and credit operations.
- Kafka enables asynchronous processing and decoupling between services.
- Transaction processing in the Ledger Service is executed within a database transaction using `@Transactional`.
- Retry support is implemented using Spring Kafka's `@RetryableTopic`.
# Author

**Bodda Bharadwaj**
