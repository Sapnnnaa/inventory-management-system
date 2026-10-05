# Inventory & Order Management System

A backend application built with **Java and Spring Boot** for managing products, warehouses, inventory, and customer orders.

The project demonstrates real-world backend concepts including **REST APIs, PostgreSQL, Spring Data JPA, transactions, stock reservation, Redis caching, Apache Kafka event-driven communication, Docker, and JWT-based authentication**.

---

## 🚀 Features

* Product management

  * Create product
  * Get product
  * Update product
  * Delete product
  * Search products

* Warehouse management

  * Create and manage warehouses
  * Associate inventory with warehouses

* Inventory management

  * Track product stock
  * Check stock availability
  * Reserve stock
  * Prevent insufficient-stock orders

* Order management

  * Create customer orders
  * Calculate order totals
  * Manage order status
  * Reserve inventory during order processing

* Database transactions

  * `@Transactional` used for business operations
  * Maintains consistency between orders and inventory

* Redis caching

  * Caches frequently accessed product data
  * Reduces repeated database queries

* Apache Kafka

  * Publishes product and order events
  * Consumes events asynchronously
  * Demonstrates event-driven architecture
  * Retry and Dead Letter Topic (DLT) handling

* Authentication and Security

  * User registration
  * Login
  * Password encryption using BCrypt
  * JWT-based authentication
  * Protected REST APIs

* API documentation

  * Swagger / OpenAPI

* Docker

  * Kafka and supporting infrastructure run using Docker

---

## 🛠️ Technologies Used

| Technology        | Purpose                          |
| ----------------- | -------------------------------- |
| Java 21           | Backend programming              |
| Spring Boot       | Application framework            |
| Spring MVC        | REST API development             |
| Spring Data JPA   | Database interaction             |
| Hibernate         | ORM                              |
| PostgreSQL        | Relational database              |
| Spring Security   | Authentication and authorization |
| JWT               | Stateless authentication         |
| Redis             | Caching                          |
| Apache Kafka      | Event-driven communication       |
| Docker            | Containerization                 |
| Maven             | Build and dependency management  |
| Swagger / OpenAPI | API documentation                |
| Postman           | API testing                      |
| Git / GitHub      | Version control                  |

---

## 🏗️ Architecture

The application follows a layered backend architecture:

```text
Client / Postman / Swagger
          |
          v
     Controller Layer
          |
          v
      Service Layer
          |
          v
    Repository Layer
          |
          v
      PostgreSQL
```

Additional infrastructure:

```text
                 ┌──────────────┐
                 │   Client     │
                 └──────┬───────┘
                        |
                        v
              ┌──────────────────┐
              │ Spring Boot API  │
              └────────┬─────────┘
                       |
          ┌────────────┼────────────┐
          |            |            |
          v            v            v
     PostgreSQL      Redis        Kafka
          |            |            |
          |            |            v
          |            |       Event Consumers
          |            |
          |            v
          |       Cached Data
          |
          v
      Business Data
```

---

## 📦 Main Modules

### Product

Responsible for managing product information.

Example:

```text
Product
 ├── id
 ├── name
 ├── sku
 ├── description
 └── price
```

### Warehouse

Manages warehouse information.

Example:

```text
Warehouse
 ├── id
 ├── name
 └── code
```

### Inventory

Tracks the quantity of products available in each warehouse.

Example:

```text
Inventory
 ├── product
 ├── warehouse
 ├── quantity
 └── reservedQuantity
```

### Order

Handles customer orders and order items.

The order process includes:

```text
Create Order
     ↓
Validate Product
     ↓
Check Warehouse
     ↓
Check Inventory
     ↓
Check Available Stock
     ↓
Reserve Stock
     ↓
Create Order
     ↓
Publish Event
```

---

## 🔐 Authentication Flow

The application uses Spring Security and JWT.

```text
Register
   ↓
Password encoded using BCrypt
   ↓
User saved in PostgreSQL
```

Login:

```text
Login Request
     ↓
Validate Username
     ↓
Validate Password
     ↓
Generate JWT
     ↓
Return JWT to Client
```

For protected APIs:

```text
Client
  |
  | Authorization: Bearer <JWT>
  v
JWT Filter
  |
  v
Validate Token
  |
  v
Authenticate User
  |
  v
Controller
```

---

## ⚡ Redis Caching

Redis is used to cache frequently accessed product information.

Example flow:

```text
GET Product
     |
     v
Check Redis
   /     \
Found    Not Found
 |          |
 v          v
Return    PostgreSQL
              |
              v
          Store in Redis
              |
              v
          Return Product
```

This reduces unnecessary database queries for frequently accessed data.

---

## 📨 Kafka Event-Driven Architecture

Apache Kafka is used for asynchronous event communication.

Example:

```text
Product Created
      |
      v
Kafka Producer
      |
      v
product-created Topic
      |
      v
Kafka Consumer
      |
      v
Process Event
```

The project also demonstrates:

* Kafka producers
* Kafka consumers
* Consumer groups
* JSON event serialization
* Retry topic
* Dead Letter Topic (DLT)
* Asynchronous processing

---

## 🗄️ Database

The project uses PostgreSQL.

Main entities include:

```text
User
Product
Warehouse
Inventory
Order
OrderItem
```

The application uses:

* Spring Data JPA
* Hibernate
* Entity relationships
* Transactions
* Database constraints

---

## 🔄 Transaction Management

Order creation uses Spring's transaction management.

Example:

```java
@Transactional
public OrderResponse createOrder(...) {
    // Validate product

    // Check inventory

    // Reserve stock

    // Create order

    // Save order
}
```

If an operation fails during the transaction, the database changes can be rolled back to maintain consistency.

---

## 📚 API Documentation

Swagger / OpenAPI is used to document and test REST APIs.

After starting the application, open:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## ▶️ How to Run the Project

### 1. Clone the repository

```bash
git clone https://github.com/Sapnnnaa/inventory-management-system.git
```

### 2. Open the project

Open the project in IntelliJ IDEA.

### 3. Configure PostgreSQL

Create a PostgreSQL database:

```sql
CREATE DATABASE inventory_db;
```

Configure your local database credentials.

For security, do not commit real database passwords to GitHub.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/inventory_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

Set `DB_PASSWORD` as an environment variable in your local environment or IntelliJ Run Configuration.

### 4. Start Redis

Make sure Redis is running on:

```text
localhost:6379
```

### 5. Start Kafka

Make sure Kafka is running on:

```text
localhost:9092
```

Kafka can be run using Docker.

### 6. Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or run:

```text
InventoryManagementSystemApplication.java
```

from IntelliJ IDEA.

---

## 🧪 Testing

The APIs can be tested using:

* Swagger UI
* Postman
* Spring Boot tests

Example API endpoints:

```text
GET    /api/products
POST   /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
DELETE /api/products/{id}

POST   /api/auth/register
POST   /api/auth/login
```

---

## 📌 Learning Objectives

This project was built to understand and practice real-world Java backend development concepts:

* Java and OOP
* Spring Boot
* REST API development
* Layered architecture
* Dependency Injection
* Spring Data JPA
* Hibernate
* PostgreSQL
* Transactions
* Exception handling
* Spring Security
* JWT authentication
* Redis caching
* Apache Kafka
* Event-driven architecture
* Docker
* API testing
* Git and GitHub

---

## 🔮 Future Improvements

Possible future enhancements:

* Role-based authorization
* Pagination and advanced filtering
* Optimistic/pessimistic locking improvements
* Distributed tracing
* Monitoring with Actuator and Prometheus
* Centralized logging
* CI/CD pipeline
* Kubernetes deployment
* Unit and integration test coverage

---

## 👩‍💻 Author

**Sapna Kumari**

Java Backend Developer

GitHub: [Sapnnnaa](https://github.com/Sapnnnaa)

---

## ⭐ Project Status

🚧 This project is continuously being improved as part of my learning and practical backend development journey.
