# 🛒 E-Commerce Microservices

A backend E-Commerce application built with **Java 21, Spring Boot, Spring Cloud, and Microservices Architecture**.

The project is organized into independent services for users, products, carts, orders, inventory, payments, and notifications. It uses **Eureka** for service discovery, **Spring Cloud Gateway** as the API entry point, **OpenFeign** for synchronous service-to-service communication, **Apache Kafka** for event-driven communication, **Redis** for JWT blacklisting and rate limiting, **Razorpay** for payments, **MySQL** for persistence, and **Docker Compose** for container orchestration.

> 
---

## 🏗️ Architecture

```text
                         ┌───────────────────┐
                         │      Client       │
                         │  REST / Frontend  │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │    API Gateway    │
                         │ Spring Cloud GW   │
                         │ JWT + Rate Limit  │
                         └─────────┬─────────┘
                                   │
                         ┌─────────▼─────────┐
                         │   Eureka Server   │
                         │ Service Discovery │
                         └─────────┬─────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
          ▼                        ▼                        ▼
   ┌─────────────┐          ┌─────────────┐          ┌─────────────┐
   │ User        │          │ Product     │          │ Cart        │
   │ Service     │          │ Service     │◄─Feign───│ Service     │
   └──────┬──────┘          └──────┬──────┘          └─────────────┘
          │                        │
          │                        │ Kafka
          │                        ▼
          │                 ┌─────────────┐
          │                 │  Inventory  │
          │                 │   Service   │
          │                 └─────────────┘
          │
          ▼
       Redis
   JWT Blacklist
   Login Rate Limit

   ┌─────────────┐       Kafka       ┌─────────────┐
   │ Order       │ ────────────────► │ Payment     │
   │ Service     │                   │ Service     │
   └──────┬──────┘                   └──────┬──────┘
          │                                  │
          │ Kafka                            ▼
          │                              Razorpay
          ▼
   ┌─────────────┐
   │ Notification│
   │   Service   │
   └─────────────┘
```

---

# 🚀 Services

The project contains **9 application services**:

| Service | Port | Responsibility |
|---|---:|---|
| **Eureka Server** | `8761` | Service discovery and registration |
| **API Gateway** | `8084` | Central API entry point, routing, JWT validation and rate limiting |
| **User Service** | `8081` | Registration, login, JWT generation, users and password recovery |
| **Product Service** | `8083` | Products, categories, subcategories and product images |
| **Order Service** | `8086` | Order creation, status, cancellation and pickup management |
| **Cart Service** | `8087` | Shopping cart management |
| **Inventory Service** | `8089` | Stock availability and stock updates |
| **Payment Service** | `8082` | Razorpay payment initiation, verification and refunds |
| **Notification Service** | `8088` | Kafka-driven notifications and email processing |

Infrastructure components:

- **MySQL** – persistent data
- **Redis** – JWT blacklist and API rate limiting
- **Apache Kafka 3.9** – event streaming
- **Docker Compose** – local container orchestration

---

# 🧰 Technology Stack

### Core

- Java 21
- Spring Boot
- Spring MVC / WebFlux
- Spring Data JPA
- Spring Security
- Maven

### Microservices

- Spring Cloud Netflix Eureka
- Spring Cloud Gateway
- Spring Cloud OpenFeign
- Spring Cloud LoadBalancer
- Resilience4j

### Messaging & Caching

- Apache Kafka
- Redis

### Database & Storage

- MySQL
- AWS S3 for product image uploads

### Payment

- Razorpay

### DevOps

- Docker
- Docker Compose

### Other

- JWT / JJWT
- JavaMail / Gmail SMTP
- Thymeleaf
- Actuator

---

# 🔐 Authentication & Authorization

The application uses **JWT-based authentication** with **role-based authorization**.

## Login Flow

```text
Client
  │
  │ POST /api/users/login
  ▼
API Gateway
  │
  ▼
User Service
  │
  │ Validate credentials
  │ Generate JWT
  ▼
Client
```

The token is returned to the client and is sent with protected requests:

```http
Authorization: Bearer <JWT>
```

The JWT contains information such as:

- User ID
- Email
- Role

The User Service uses Spring Security with a stateless session policy and a custom JWT authentication filter.

---

# 🛡️ Microservice-Level Authorization

The services do not rely only on the gateway for authorization.

The downstream services contain their own Spring Security configuration and establish the authenticated user from headers added by the API Gateway:

```text
X-User-Id
X-User-Role
X-User-Email
```

The gateway:

1. Reads the JWT.
2. Checks the Redis blacklist.
3. Validates the JWT.
4. Extracts user ID, role and email.
5. Adds authenticated-user headers to the downstream request.

The microservices then create a Spring Security `Authentication` object from these headers and use method-level authorization such as:

```java
@PreAuthorize("hasRole('ADMIN')")
```

and:

```java
@PreAuthorize("hasRole('USER')")
```

### Examples implemented in the project

**Product Service**
- Product administration → `ADMIN`
- Product browsing → `USER` / `ADMIN`

**Cart Service**
- Cart operations → `USER`

**Order Service**
- Customer order operations → `USER`
- Administrative order/status operations → `ADMIN`

**Payment Service**
- Customer payment operations → `USER`
- Administrative operations → `ADMIN`

**Inventory Service**
- Stock administration → `ADMIN`

This provides authorization at the **individual microservice/controller level**.

---

# 🚪 API Gateway

The API Gateway is implemented using **Spring Cloud Gateway**.

It provides routes for:

```text
/api/users/**
/api/product/**
/api/order/**
/api/inventory/**
/api/cart/**
/api/payments/**
```

The routes use Eureka/LoadBalancer service names such as:

```text
lb://user-service
lb://product-service
lb://order-service
lb://inventory-service
lb://cart-service
lb://payment-service
```

This avoids hardcoding the target service's host/IP address in the gateway.

---

# 🔴 Redis

Redis is used for two important purposes.

## 1. JWT Blacklisting

JWTs are normally stateless, so logging out does not automatically invalidate an already-issued token.

This project stores logged-out JWTs in Redis.

```text
User Logout
     │
     ▼
User Service
     │
     ▼
Redis Blacklist
```

For every protected gateway request:

```text
JWT
 │
 ▼
Redis blacklist check
 │
 ├── Blacklisted → 401 Unauthorized
 │
 └── Not blacklisted
          │
          ▼
     Validate JWT
          │
          ▼
     Forward request
```

This provides immediate JWT invalidation after logout.

## 2. Login Rate Limiting

The API Gateway also uses Redis-backed Spring Cloud Gateway rate limiting for the login endpoint.

The implemented resolver combines:

```text
IP Address + Username
```

into a rate-limit key.

This helps reduce brute-force attempts against individual accounts.

The configured login limiter uses:

```text
Replenish Rate: 3
Burst Capacity: 5
Requested Tokens: 1
```

---

# 📨 Apache Kafka

Kafka is used for **asynchronous event streaming**.

The project uses event-driven communication for operations such as:

- Product creation
- Stock reduction
- Stock restoration
- Order placement
- Payment success
- Payment failure
- Order status changes
- Refund completion

## Kafka Topics

The current code contains the following important topics:

| Topic | Producer | Consumer(s) |
|---|---|---|
| `product-event` | Product Service | Inventory Service |
| `product-created-topic` | Product Service | Inventory Service |
| `Update-Stock` | Inventory Service | Product Service |
| `order-placed` | Order Service | Payment Service, Notification Service |
| `REDUCE-STOCK` | Order Service | Inventory Service |
| `RESTORE-STOCK` | Order Service | Inventory Service |
| `payment-success` | Payment Service | Notification Service |
| `payment-failed` | Payment Service | Notification Service |
| `refund-done` | Payment Service | Payment-side event flow |
| `order-status-changed` | Order-side event flow | Notification Service |

> Topic names are taken from the current source code and may evolve as the application is extended.

---

# 🔄 Order & Event-Driven Flow

A simplified order flow implemented by the services is:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Order Service
  │
  ├── OpenFeign ──► Cart Service
  │
  ├── OpenFeign ──► Inventory Service
  │
  └── Kafka: order-placed
                 │
          ┌──────┴──────┐
          ▼             ▼
   Payment Service  Notification
          │             Service
          │
          ▼
       Razorpay
```

Stock-related events are handled asynchronously:

```text
Order Service
     │
     ├── REDUCE-STOCK ──► Inventory Service
     │
     └── RESTORE-STOCK ─► Inventory Service
```

This separates synchronous business operations from asynchronous event processing.

---

# 🔗 OpenFeign Communication

The project uses **Spring Cloud OpenFeign** for synchronous communication.

Examples from the implementation:

### Cart → Product

```text
Cart Service
     │
     │ OpenFeign
     ▼
Product Service
```

### Order → Cart

```text
Order Service
     │
     │ OpenFeign
     ▼
Cart Service
```

### Order → Inventory

```text
Order Service
     │
     │ OpenFeign
     ▼
Inventory Service
```

### Payment → Order

```text
Payment Service
     │
     │ OpenFeign
     ▼
Order Service
```

This gives the system a combination of:

- **Synchronous communication** → OpenFeign
- **Asynchronous communication** → Kafka

---

# 🛒 Cart Service

The Cart Service manages customer shopping carts.

Implemented APIs include operations for:

```text
GET    /api/cart
POST   /api/cart/add
PUT    /api/cart/items/{cartItemId}
DELETE /api/cart/items/{cartItemId}
DELETE /api/cart/clear
GET    /api/cart/count
GET    /api/cart/items/{cartItemId}
```

Cart operations are protected using the `USER` role.

---

# 📦 Product Service

The Product Service manages:

- Products
- Categories
- Subcategories
- Product search
- Product filtering
- Advanced product filtering
- Product images

Main API groups:

```text
/api/product/**
/api/product/categories/**
/api/product/subCategories/**
/api/product/images/**
```

Product and category administration is protected using role-based authorization.

---

# 🖼️ Product Image Storage

The Product Service contains an AWS S3 integration for product image uploads.

The implementation uses the AWS SDK `S3Client` and uploads objects to an S3 bucket.

Conceptually:

```text
Client
  │
  │ Upload Product Image
  ▼
Product Service
  │
  ▼
AWS S3
  │
  ▼
Stored Product Image
```

AWS credentials and bucket configuration should be supplied through secure environment-specific configuration.

---

# 📦 Inventory Service

The Inventory Service manages product stock.

It supports:

```text
GET  /api/inventory/check/{productId}/{quantity}
POST /api/inventory
POST /api/inventory/update
```

Kafka consumers process:

```text
product-event
REDUCE-STOCK
RESTORE-STOCK
```

This allows inventory changes to be processed asynchronously from order events.

---

# 💳 Payment Service

The Payment Service integrates with **Razorpay**.

Main endpoints include:

```text
POST /api/payments/initiate
POST /api/payments/verify
GET  /api/payments/refund/{orderId}
GET  /api/payments/order/{orderNumber}
GET  /api/payments/my-payments
GET  /api/payments
POST /api/payments/webhook
GET  /api/payments/refund-status/{orderNumber}
```

## Payment Flow

```text
Client
  │
  ▼
Payment Service
  │
  │ Create payment
  ▼
Razorpay
  │
  │ Payment result
  ▼
Payment Service
  │
  ├── payment-success ──► Kafka
  │
  └── payment-failed ───► Kafka
```

The payment service also contains a Razorpay webhook endpoint.

---

# 🔔 Notification Service

The Notification Service consumes Kafka events and handles notification/email processing.

It listens for events such as:

```text
order-placed
payment-success
payment-failed
order-status-changed
```

It uses Spring Mail and Gmail SMTP for email delivery.

The service also exposes notification APIs under:

```text
/api/notifications/**
```

---

# 🧯 Resilience4j

The project contains **Resilience4j Circuit Breaker** configuration in services that perform inter-service communication.

For example, the Order Service defines circuit breakers for:

```text
productService
inventoryService
```

Configuration includes:

```text
Sliding Window Size       : 10
Minimum Number of Calls   : 5
Failure Rate Threshold    : 50%
Open State Wait Duration  : 10 seconds
Half-Open Calls           : 3
```

This helps prevent repeated calls to an unhealthy downstream service.

---

# 🗄️ Database Design

The services use MySQL with separate databases for service-specific data.

The current configuration references:

```text
user_db
product_db
Cart_db
order_db
inventory_db
notification_db
payment_db
```

This follows the microservice principle of keeping service data logically separated.

---

# 🐳 Docker & Docker Compose

The repository contains Dockerfiles for the individual services and a root `docker-compose.yml`.

The Compose environment includes containers for:

```text
eureka-server
api-gateway
user-service
product-service
cart-service
order-service
inventory-service
payment-service
notification-service
redis
kafka
```

Kafka is configured in **KRaft mode** with a single broker/controller for the development environment.

```text
Kafka
 ├── Broker
 └── Controller
```

---

# ⚙️ Configuration

The project contains separate configuration files for local and Docker environments.

Examples:

```text
application-local.yml
application-docker.yml
```

Typical local configuration uses:

```text
localhost
```

while Docker configuration uses Docker service names such as:

```text
redis
kafka
eureka-server
```

For example:

```text
Local Kafka:
localhost:9092

Docker Kafka:
kafka:9092
```

and:

```text
Local Redis:
localhost:6379

Docker Redis:
redis:6379
```

---

# 🔑 Environment & Secrets

Sensitive credentials should **not** be committed to source control.

The repository currently contains configuration examples with credentials for:

- MySQL
- Gmail SMTP
- JWT
- Razorpay
- AWS S3

For a production deployment, move these values to:

- Environment variables
- Docker secrets
- AWS Secrets Manager
- Kubernetes Secrets
- Another secure configuration provider

Example:

```env
MYSQL_USERNAME=<username>
MYSQL_PASSWORD=<password>

JWT_SECRET=<secret>

RAZORPAY_KEY_ID=<key>
RAZORPAY_KEY_SECRET=<secret>

AWS_ACCESS_KEY=<key>
AWS_SECRET_KEY=<secret>
AWS_REGION=<region>
AWS_S3_BUCKET=<bucket>

MAIL_USERNAME=<email>
MAIL_PASSWORD=<app-password>
```

**Never commit real credentials to GitHub.**

---

# ▶️ Running the Project

## Prerequisites

Install:

- Java 21
- Maven
- Docker
- Docker Compose
- MySQL
- Git

---

## 1. Prepare MySQL

The current Docker Compose configuration does **not** start a MySQL container. The Docker profiles connect to MySQL through:

```text
host.docker.internal:3306
```

Create the required databases:

```sql
CREATE DATABASE user_db;
CREATE DATABASE product_db;
CREATE DATABASE Cart_db;
CREATE DATABASE order_db;
CREATE DATABASE inventory_db;
CREATE DATABASE notification_db;
CREATE DATABASE payment_db;
```

Update the credentials in the appropriate configuration before starting the services.

---

## 2. Start Kafka and Redis

They are included in the root Docker Compose configuration.

```bash
docker compose up -d redis kafka
```

Verify:

```bash
docker compose ps
```

---

## 3. Start the Services

After correcting the Docker Compose build paths/configuration for the current repository layout:

```bash
docker compose up -d --build
```

Check logs:

```bash
docker compose logs -f
```

Check a specific service:

```bash
docker compose logs -f user-service
```

Stop the environment:

```bash
docker compose down
```

---

# 🌐 Important URLs

After the services are running:

| Component | URL |
|---|---|
| Eureka Dashboard | `http://localhost:8761` |
| API Gateway | `http://localhost:8084` |
| User Service | `http://localhost:8081` |
| Product Service | `http://localhost:8083` |
| Payment Service | `http://localhost:8082` |
| Order Service | `http://localhost:8086` |
| Cart Service | `http://localhost:8087` |
| Notification Service | `http://localhost:8088` |
| Inventory Service | `http://localhost:8089` |
| Redis | `localhost:6379` |
| Kafka | `localhost:9092` |

For normal client access, use the **API Gateway** rather than calling the microservices directly.

---

# 🧪 Example API Flow

### 1. Register

```http
POST /api/users/register
```

### 2. Login

```http
POST /api/users/login
```

Receive a JWT.

### 3. Send JWT

```http
Authorization: Bearer <JWT>
```

### 4. Browse Products

```http
GET /api/product
```

### 5. Add Product to Cart

```http
POST /api/cart/add
```

### 6. Place Order

```http
POST /api/order
```

### 7. Initiate Payment

```http
POST /api/payments/initiate
```

### 8. Verify Payment

```http
POST /api/payments/verify
```

During this process, Kafka events are used for stock and notification processing.

---

# 📁 Repository Structure

```text
Ecommerce-microservice/
│
├── api-gateway/
│
├── Cart-service/
│
├── Eureka-Server/
│
├── Inventry-Service/
│
├── Notification-Service/
│
├── Order-Service/
│
├── Payment-Service/
│
├── Product-Service/
│
├── User-Service/
│
├── Kafka-file/
├── kafka-infra/
│
├── docker-compose.yml
│
└── README.md
```

---

# 🎯 Key Technical Highlights

This project demonstrates practical experience with:

- **Microservices Architecture**
- **Spring Boot**
- **Spring Cloud**
- **Eureka Service Discovery**
- **Spring Cloud API Gateway**
- **OpenFeign**
- **Spring Cloud LoadBalancer**
- **JWT Authentication**
- **Role-Based Authorization**
- **Method-Level Security with `@PreAuthorize`**
- **Redis JWT Blacklisting**
- **Redis-backed Login Rate Limiting**
- **Apache Kafka Event Streaming**
- **Event-driven Architecture**
- **Resilience4j Circuit Breaker**
- **Razorpay Payment Integration**
- **AWS S3 Product Image Storage**
- **MySQL**
- **Docker**
- **Docker Compose**
- **Email Notifications**
- **Spring Actuator**

---

# 💡 Why This Architecture?

### Why Microservices?

The application is divided into business domains so that services can be developed, maintained, scaled, and deployed independently.

### Why Eureka?

Eureka removes the need for hardcoded service addresses and provides service discovery.

### Why OpenFeign?

OpenFeign simplifies synchronous HTTP communication between services using declarative clients.

### Why Kafka?

Kafka provides asynchronous event streaming and reduces tight coupling between services.

### Why Redis?

Redis provides fast access for JWT blacklist checks and is also used by the gateway for login rate limiting.

### Why JWT?

JWT provides stateless authentication suitable for distributed microservices.

### Why Razorpay?

Razorpay provides payment processing capabilities for the application's online checkout flow.

### Why Docker Compose?

Docker Compose provides a convenient way to run the microservices and infrastructure together in a development environment.

---
---

# 🚀 Future Improvements

Potential next steps for the project:

- Kubernetes deployment
- CI/CD with GitHub Actions
- AWS ECS/EKS deployment
- AWS Secrets Manager integration
- Centralized configuration with Spring Cloud Config
- Distributed tracing with OpenTelemetry
- Prometheus + Grafana monitoring
- Centralized logging
- API documentation using Swagger/OpenAPI
- Dedicated MySQL containers/RDS per environment
- Kafka multi-broker production setup
- HTTPS/TLS configuration
- Container image publishing to Docker Hub/AWS ECR

---

# 👨‍💻 Project Summary

This E-Commerce project demonstrates how a distributed backend can combine **synchronous REST communication, asynchronous Kafka events, centralized gateway routing, service discovery, JWT security, role-based authorization, Redis, payment integration, and containerization** into a single microservices-based application.

The architecture is designed around independent business services while using shared infrastructure components for communication, security, messaging, caching, and deployment.
