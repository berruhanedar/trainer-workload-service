# Gym CRM - Microservices

A Spring Boot based Gym CRM application extended with a microservices architecture.

The project consists of the main Gym CRM application, a dedicated Trainer Workload microservice, and a Eureka Discovery Service.

The main purpose of the microservice integration is to calculate and maintain the monthly workload of trainers whenever a training session is created or deleted.

---

# Project Overview

The system is designed around three applications:

1. **Gym Spring Boot**
    - Main Gym CRM application
    - Manages trainees, trainers and trainings
    - Handles authentication and authorization
    - Sends trainer workload information to the workload microservice

2. **Trainer Workload Service**
    - Dedicated microservice for trainer workload calculation
    - Receives training workload events
    - Calculates monthly training duration
    - Stores the calculated workload in an in-memory H2 database

3. **Eureka Discovery Service**
    - Provides service discovery
    - Allows microservices to find each other by service name
    - Removes the need to use hardcoded service addresses

---

# Architecture

```text
                           ┌──────────────────┐
                           │     Postman      │
                           └────────┬─────────┘
                                    │
                                    │ HTTP
                                    │ JWT Bearer
                                    ▼
                         ┌──────────────────────┐
                         │   Gym Spring Boot    │
                         │       :8080          │
                         └──────────┬───────────┘
                                    │
                                    │ Service Discovery
                                    │
                         ┌──────────▼───────────┐
                         │    Eureka Server     │
                         │        :8761         │
                         └──────────┬───────────┘
                                    │
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Trainer Workload     │
                         │      Service        │
                         │       :8081          │
                         └──────────┬───────────┘
                                    │
                                    ▼
                              ┌───────────┐
                              │ H2 Memory │
                              │ Database  │
                              └───────────┘
```

---

# Applications

| Application | Port | Description |
|---|---:|---|
| Gym Spring Boot | `8080` | Main Gym CRM REST API |
| Trainer Workload Service | `8081` | Trainer workload calculation |
| Eureka Discovery Service | `8761` | Service discovery |

---

# 1. Gym Spring Boot

The Gym Spring Boot application is the main application of the system.

It provides the core Gym CRM functionality.

## Main Responsibilities

- Trainee management
- Trainer management
- Training management
- Training type management
- Authentication
- JWT authorization
- Database operations
- REST API
- Communication with Trainer Workload Service
- Circuit Breaker handling
- Transaction tracking
- Application logging
- Monitoring

---

# 2. Trainer Workload Service

The Trainer Workload Service is a separate Spring Boot application.

Its responsibility is to calculate the amount of training time performed by each trainer for each month.

Whenever a training is created or deleted in the Gym application, the corresponding workload information is sent to this service.

---

# Trainer Workload Request

The workload service accepts the following information:

| Field | Description |
|---|---|
| `trainerUsername` | Trainer username |
| `trainerFirstName` | Trainer first name |
| `trainerLastName` | Trainer last name |
| `isActive` | Trainer active status |
| `trainingDate` | Training date |
| `trainingDuration` | Training duration |
| `actionType` | `ADD` or `DELETE` |

Example request:

```json
{
  "trainerUsername": "Mike.Smith",
  "trainerFirstName": "Mike",
  "trainerLastName": "Smith",
  "isActive": true,
  "trainingDate": "2026-08-08",
  "trainingDuration": 60,
  "actionType": "ADD"
}
```

---

# Workload Calculation

The service keeps trainer workload in the following structure:

```text
TrainerWorkload
│
├── Trainer Username
├── Trainer First Name
├── Trainer Last Name
├── Trainer Status
│
└── Years
      │
      └── Year
           │
           └── Months
                │
                ├── Month
                └── Training Summary Duration
```

For example:

```text
Mike.Smith
│
└── 2026
     │
     └── August
          │
          └── 180 minutes
```

---

# ADD Operation

When a new training is created, Gym sends:

```text
ActionType = ADD
```

The workload service increases the monthly duration.

Example:

```text
Existing workload = 120 minutes

New training = 60 minutes

120 + 60 = 180 minutes
```

---

# DELETE Operation

When a training is deleted, Gym sends:

```text
ActionType = DELETE
```

The workload service decreases the monthly duration.

Example:

```text
Existing workload = 180 minutes

Deleted training = 60 minutes

180 - 60 = 120 minutes
```

The implementation also prevents the calculated duration from becoming negative.

---

# Gym → Workload Communication

When a training is created:

```text
POST /api/trainings
        │
        ▼
Gym Spring Boot
        │
        │ ADD
        ▼
Trainer Workload Service
        │
        ▼
Monthly workload updated
```

When a training is deleted:

```text
DELETE /api/trainings/{trainingId}
        │
        ▼
Gym Spring Boot
        │
        │ DELETE
        ▼
Trainer Workload Service
        │
        ▼
Monthly workload updated
```

---

# REST API

The API follows the second level of the Richardson Maturity Model.

The implementation uses:

- Resource-oriented URLs
- HTTP methods
- HTTP status codes

HATEOAS is not required because the project targets Level 2 rather than Level 3.

---

## Training Endpoints

### Create Training

```http
POST /api/trainings
```

Creates a new training.

The operation also sends an `ADD` workload request to the Trainer Workload Service.

---

### Delete Training

```http
DELETE /api/trainings/{trainingId}
```

Deletes an existing training.

The operation also sends a `DELETE` workload request to the Trainer Workload Service.

---

### Get Trainer Trainings

```http
GET /api/trainings/trainers/{username}/trainings
```

Returns the trainings belonging to the specified trainer.

---

### Get Trainee Trainings

```http
GET /api/trainings/trainees/{username}/trainings
```

Returns the trainings belonging to the specified trainee.

---

### Get Training Types

```http
GET /api/trainings/types
```

Returns the available training types.

---

# Trainer Workload Endpoint

```http
POST /api/workloads
```

Receives trainer workload information from the Gym application.

Successful requests return:

```text
200 OK
```

---

# Service Discovery with Eureka

The project uses Netflix Eureka for service discovery.

Both the Gym application and Trainer Workload Service register themselves with Eureka.

The Gym application communicates with the workload service using its service name:

```text
http://trainer-workload-service
```

instead of using a hardcoded address such as:

```text
http://localhost:8081
```

This allows the service location to be resolved dynamically through Eureka.

---

## Eureka Server

The Eureka server runs on:

```text
http://localhost:8761
```

The Eureka dashboard can be used to verify registered services.

Expected services include:

```text
GYM-SPRINGBOOT
TRAINER-WORKLOAD-SERVICE
```

---

# Circuit Breaker

The communication between Gym Spring Boot and Trainer Workload Service uses the Circuit Breaker design pattern.

The Circuit Breaker protects the main application when the workload service becomes unavailable.

The configuration contains:

- Sliding window size
- Minimum number of calls
- Failure rate threshold
- Open state duration
- Half-open state calls
- Automatic transition
- TimeLimiter

Example:

```properties
resilience4j.circuitbreaker.instances.trainerWorkloadService.sliding-window-size=5
resilience4j.circuitbreaker.instances.trainerWorkloadService.minimum-number-of-calls=3
resilience4j.circuitbreaker.instances.trainerWorkloadService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.trainerWorkloadService.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.trainerWorkloadService.permitted-number-of-calls-in-half-open-state=2
resilience4j.circuitbreaker.instances.trainerWorkloadService.automatic-transition-from-open-to-half-open-enabled=true
```

When the workload service is unavailable, the fallback method is executed.

Example log:

```text
Trainer workload service is unavailable.
trainerUsername=Mike.Smith
actionType=ADD
```

This prevents a failure in the secondary microservice from directly breaking the main training operation.

---

# JWT Authorization

The microservice communication is secured using JWT Bearer authentication.

Authenticated requests use:

```http
Authorization: Bearer <JWT_TOKEN>
```

The Gym application validates the JWT token.

When Gym communicates with the Trainer Workload Service, the same Authorization header is propagated to the downstream service.

```text
Client
  │
  │ Authorization: Bearer JWT
  ▼
Gym Spring Boot
  │
  │ Authorization: Bearer JWT
  ▼
Trainer Workload Service
```

The Trainer Workload Service also validates the received JWT.

---

# Transaction ID Tracking

The application implements transaction-level tracking using a unique `transactionId`.

For every incoming request, a transaction ID is:

- Read from the request if already provided
- Generated if it does not exist
- Stored in MDC
- Added to the response
- Passed to downstream services

The transaction ID is propagated using:

```http
X-Transaction-Id
```

---

## Transaction Flow

Example:

```text
Client
   │
   │ POST /api/trainings
   ▼
Gym Spring Boot
   │
   │ transactionId:
   │ d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca
   │
   │ X-Transaction-Id
   ▼
Trainer Workload Service
```

Both applications therefore use the same transaction ID.

Example Gym log:

```text
[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
Training created successfully. id=1
```

Example Workload log:

```text
[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
Processing trainer workload.
```

This makes it possible to trace a single request across multiple services.

---

# Logging

Two levels of logging are implemented.

## Transaction Level

The transaction level records information such as:

- Transaction ID
- Endpoint
- HTTP method
- Request
- Response
- HTTP status

Example:

```text
REST request:
method=POST
endpoint=/api/trainings
```

and:

```text
REST response:
status=200
```

---

## Operation Level

The operation level records important business operations.

Example:

```text
Creating training.
traineeUsername=Berru.Hanedar
trainerUsername=Mike.Smith
```

and:

```text
Trainer workload processed successfully.
trainerUsername=Mike.Smith
year=2026
month=8
totalDuration=60
```

---

# Example End-to-End Flow

A complete training creation request works as follows:

```text
1. Client sends POST /api/trainings
              │
              ▼
2. Gym validates JWT
              │
              ▼
3. Gym generates / retrieves transactionId
              │
              ▼
4. Training is saved to the Gym database
              │
              ▼
5. TrainerWorkloadClient is called
              │
              ▼
6. Circuit Breaker executes downstream call
              │
              ▼
7. JWT and transactionId are propagated
              │
              ▼
8. Eureka resolves trainer-workload-service
              │
              ▼
9. Trainer Workload Service receives request
              │
              ▼
10. Workload is calculated
              │
              ▼
11. Monthly summary is updated
              │
              ▼
12. Operation is logged with the same transactionId
```

---

# Example Successful Logs

## Gym Spring Boot

```text
[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
REST request: method=POST, endpoint=/api/trainings

[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
Creating training.
traineeUsername=Berru.Hanedar,
trainerUsername=Mike.Smith

[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
Training created successfully. id=1

[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
REST response: status=200
```

## Trainer Workload Service

```text
[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
Processing trainer workload.
trainerUsername=Mike.Smith,
trainingDate=2026-08-08,
duration=60,
actionType=ADD

[d6ac94a9-8b0f-4e8a-ae5c-36638294f2ca]
Trainer workload processed successfully.
trainerUsername=Mike.Smith,
year=2026,
month=8,
totalDuration=60
```

The matching transaction ID demonstrates that both services belong to the same request flow.

---

# Security Flow

```text
                    JWT
                     │
                     ▼
              ┌──────────────┐
              │     Client   │
              └──────┬───────┘
                     │
                     │ Bearer Token
                     ▼
              ┌──────────────┐
              │     Gym      │
              └──────┬───────┘
                     │
                     │ Bearer Token
                     ▼
              ┌──────────────┐
              │   Workload   │
              │    Service   │
              └──────────────┘
```

The token is therefore preserved during microservice-to-microservice communication.

---

# Technology Stack

## Language

- Java 21

## Frameworks

- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Cloud

## Microservices

- Netflix Eureka
- Resilience4j
- JWT

## Database

- H2 In-Memory Database

## Libraries

- Lombok
- MapStruct
- JJWT

## Monitoring

- Spring Boot Actuator
- Micrometer
- Prometheus

## API Documentation

- Swagger / OpenAPI

## Build Tool

- Maven

---

# Project Structure

## Gym Spring Boot

```text
gym-springboot/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.berruhanedar.app.gym_springboot/
│   │   │       ├── client/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dao/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── facade/
│   │   │       ├── filter/
│   │   │       ├── mapper/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │
│   └── test/
│
└── pom.xml
```

---

## Trainer Workload Service

```text
trainer-workload-service/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.berruhanedar.app/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── enums/
│   │   │       ├── filter/
│   │   │       ├── mapper/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │
│   └── test/
│
└── pom.xml
```

---

## Discovery Service

```text
discovery-service/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │
│   └── test/
│
└── pom.xml
```

---

# Running the Project

The services should be started in the following order.

## Step 1 - Eureka Discovery Service

Start the Eureka Server.

```bash
./mvnw spring-boot:run
```

Eureka dashboard:

```text
http://localhost:8761
```

---

## Step 2 - Trainer Workload Service

Start the Trainer Workload Service.

### Windows

```powershell
.\mvnw spring-boot:run
```

The service runs on:

```text
http://localhost:8081
```

---

## Step 3 - Gym Spring Boot

Start the Gym application.

### Windows

```powershell
.\mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

---

# Testing with Postman

A typical integration test can be performed as follows:

```text
1. Start Eureka
        ↓
2. Start Trainer Workload Service
        ↓
3. Start Gym Spring Boot
        ↓
4. Login using /api/login
        ↓
5. Copy JWT token
        ↓
6. Send Authorization: Bearer <token>
        ↓
7. Create a training
        ↓
8. Check Gym logs
        ↓
9. Check Trainer Workload logs
        ↓
10. Verify the same transactionId
        ↓
11. Verify monthly workload
```

---

# Swagger

Swagger UI is available through the Gym application:

```text
http://localhost:8080/swagger-ui/index.html
```

The API can also be tested through Swagger after authentication.

---

# H2 Database

The Trainer Workload Service uses an in-memory H2 database.

Configuration:

```properties
spring.datasource.url=jdbc:h2:mem:workloaddb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

H2 Console:

```text
http://localhost:8081/h2-console
```

The database is intentionally configured as an in-memory database because the Trainer Workload Service is responsible for maintaining the workload information during application runtime.

---

# Design Patterns Used

The project demonstrates several common backend and microservice design patterns.

## Microservice Architecture

The workload calculation is separated from the main Gym application into its own independently running service.

## Service Discovery

Eureka allows services to locate each other dynamically.

## Circuit Breaker

Resilience4j protects the main application from failures in the downstream workload service.

## DTO Pattern

DTOs are used to transfer data between API layers and services.

## Mapper Pattern

MapStruct is used for object mapping.

## Repository Pattern

Spring Data repositories handle database access.

## Facade Pattern

`GymFacade` provides a simplified interface for application operations.

## JWT Authentication

JWT Bearer tokens are used for authentication and service-to-service authorization.

## Transaction ID Propagation

A transaction ID allows a request to be traced across multiple services.

---

# Richardson Maturity Model

The REST API follows **Level 2** of the Richardson Maturity Model.

The implementation uses:

```text
Resource-oriented URLs
        +
HTTP Methods
        +
HTTP Status Codes
```

Examples:

```http
GET /api/trainings/...
POST /api/trainings
DELETE /api/trainings/{trainingId}
```

HATEOAS is not implemented because it belongs to Level 3 of the Richardson Maturity Model and is not required by this project.

---

# Requirements Implemented

The microservices implementation covers the following requirements:

- [x] Separate Spring Boot Microservice
- [x] Trainer workload REST endpoint
- [x] Trainer workload request contract
- [x] Monthly workload calculation
- [x] In-memory database
- [x] ADD workload operation
- [x] DELETE workload operation
- [x] Main Gym application integration
- [x] Eureka Discovery Service
- [x] Circuit Breaker
- [x] JWT Bearer Authorization
- [x] Transaction-level logging
- [x] Operation-level logging
- [x] Transaction ID generation
- [x] Transaction ID propagation to downstream service
- [x] Richardson Maturity Model Level 2

---

# Project Status

The project currently contains:

```text
Gym CRM
     +
Trainer Workload Microservice
     +
Eureka Service Discovery
     +
Circuit Breaker
     +
JWT Security
     +
Transaction Tracking
     +
Centralized Logging
```

The complete training workflow can therefore be traced from the initial REST request in the Gym application through the Trainer Workload Service using the same transaction ID.