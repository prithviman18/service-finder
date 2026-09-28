# Local Service Finder — Comprehensive Project Documentation 📘

**Local Service Finder** is a web-based community service marketplace connecting local residents (**Users/Customers**) with verified skilled tradespeople (**Workers** such as plumbers, electricians, carpenters, painters, and appliance technicians) based on service type and geographic location, under the supervision of a platform **Administrator**.

---

## 📑 Table of Contents
1. [Project Overview & Objectives](#1-project-overview--objectives)
2. [System Architecture & Technology Stack](#2-system-architecture--technology-stack)
3. [Object-Oriented Programming (OOP) Implementation](#3-object-oriented-programming-oop-implementation)
4. [Role-Based Access Control & User Workflows](#4-role-based-access-control--user-workflows)
5. [Data Persistence Architecture](#5-data-persistence-architecture)
6. [REST API Specification](#6-rest-api-specification)
7. [Deployment & Run Guide](#7-deployment--run-guide)
8. [Viva Q&A Preparation Cheat Sheet](#8-viva-qa-preparation-cheat-sheet)

---

## 1. Project Overview & Objectives

### 1.1 Problem Statement
Finding reliable, localized trade professionals (e.g., plumbers in Alappuzha, electricians in Kochi) is often difficult without centralized directory services. Many platforms are overly complex or require database setups that are heavy to maintain.

### 1.2 Proposed Solution
A lightweight, zero-database full-stack web application that:
- Runs from a single runnable JAR (or Docker container).
- Persists all state cleanly in a structured JSON file (`data/users.json`).
- Adheres strictly to core Object-Oriented Programming (OOP) paradigms in Java 17.
- Delivers a responsive, modern user experience without needing heavyweight frontend frameworks like React or Node.js.

---

## 2. System Architecture & Technology Stack

### 2.1 High-Level Architecture Diagram

```
+--------------------------------------------------------------------------------+
|                          Browser Client (HTML5 / CSS3 / Vanilla JS)             |
|   - login.html               - admin.html         - worker.html    - user.html |
+--------------------------------------------------------------------------------+
                                       |
                                       | HTTP REST API (JSON via fetch API)
                                       v
+--------------------------------------------------------------------------------+
|                     Java 17 Backend Web Server (Javalin 6.x)                   |
|                                                                                |
|  +--------------------------------------------------------------------------+  |
|  | Main.java (Static File Handler + REST Controllers + Exception Handlers)  |  |
|  +--------------------------------------------------------------------------+  |
|                                       |                                        |
|  +--------------------------------------------------------------------------+  |
|  | Service Layer:                                                           |  |
|  |   - AuthService (Login, Register, Token Management)                      |  |
|  |   - WorkerService (Profile Management)                                   |  |
|  |   - AdminService (Worker/User Directories & Statistics)                  |  |
|  |   - SearchService (Privacy-Filtered Worker Search)                       |  |
|  +--------------------------------------------------------------------------+  |
|                                       |                                        |
|  +--------------------------------------------------------------------------+  |
|  | Data Layer:                                                              |  |
|  |   - Repository<T> Interface                                              |  |
|  |   - JsonStore (Singleton, synchronized read/write)                       |  |
|  +--------------------------------------------------------------------------+  |
+--------------------------------------------------------------------------------+
                                       |
                                       v
+--------------------------------------------------------------------------------+
|                         Storage: data/users.json                               |
+--------------------------------------------------------------------------------+
```

### 2.2 Technology Stack Details

| Layer | Technology | Rationale |
|---|---|---|
| **Backend Language** | Java 17 (JDK 17 LTS) | Modern Java with records, pattern matching, strong typing, and OOP capabilities. |
| **Web Framework** | Javalin 6.3.0 | Ultra-lightweight micro-framework on Jetty; handles routing, static files, and JSON with zero boilerplate. |
| **JSON Serialization** | Jackson Databind 2.17.x | Fast, industry-standard JSON parser supporting polymorphic subtype deserialization (`@JsonTypeInfo`). |
| **Logging** | SLF4J Simple 2.0.x | Simple, lightweight logging integration. |
| **Build Tool** | Apache Maven 3.9+ with Shade Plugin | Packages application and dependencies into a single runnable `app.jar`. |
| **Frontend** | HTML5, CSS3, Vanilla JS | No Node.js runtime required; fast load times, clean Fetch API asynchronous calls, responsive design. |
| **Deployment** | Docker / Render.com | Self-contained multi-stage build running on Linux JRE containers. |

---

## 3. Object-Oriented Programming (OOP) Implementation

This project was built to illustrate clean OOP principles:

### 3.1 Abstraction
- **Abstract Base Class `Person`**: Represents common user properties (`username`, `password`, `name`, `phone`) but cannot be instantiated directly.
- Defines abstract methods that every concrete user subclass must implement:
  ```java
  public abstract String getRole();
  public abstract String getDashboardPage();
  public abstract Map<String, Object> toMap(boolean includeSensitive);
  ```

### 3.2 Inheritance
- `Admin`, `Customer`, and `Worker` extend `Person`.
- Reuses common fields and behaviors from `Person` while extending specialized attributes:
  - `Worker` adds `aadhar`, `location`, and `ServiceType service`.
  - `Customer` represents a normal hiring user.
  - `Admin` represents platform administration.

### 3.3 Polymorphism
- **Dynamic Method Dispatch**: When a user logs in, the controller simply calls:
  ```java
  String dashboard = person.getDashboardPage();
  ```
  - For `Admin`, it returns `"admin.html"`.
  - For `Worker`, it returns `"worker.html"`.
  - For `Customer`, it returns `"user.html"`.
- The authentication code does not need complex `if/else` checks for page routing; it relies on polymorphism.
- `person.toMap(boolean includeSensitive)` polymorphically determines whether private attributes (like Aadhar) are serialized.

### 3.4 Encapsulation & Data Validation
- All class fields are declared `private`.
- Access is provided exclusively through getters and setters.
- Setters actively enforce business invariants:
  ```java
  public void setAadhar(String aadhar) {
      Validator.validateAadhar(aadhar); // Enforces ^\d{12}$ regex
      this.aadhar = aadhar.trim();
  }

  public void setLocation(String location) {
      Validator.validateLocation(location); // Enforces non-blank plain text
      this.location = location.trim();
  }
  ```

### 3.5 Interface
- Generic contract `Repository<T>` separates business logic from storage mechanisms:
  ```java
  public interface Repository<T> {
      List<T> findAll();
      Optional<T> findByUsername(String username);
      void save(T entity);
      void update(T entity);
      void delete(String username);
      void reload();
  }
  ```
- `JsonStore` implements `Repository<Person>`. If the system is migrated to MySQL or MongoDB in the future, only a new repository implementation is needed without touching service layers.

### 3.6 Singleton Design Pattern
- `JsonStore` ensures that only one repository instance interacts with `data/users.json` across all incoming HTTP threads:
  ```java
  public static JsonStore getInstance() {
      if (instance == null) {
          synchronized (JsonStore.class) {
              if (instance == null) {
                  instance = new JsonStore("data/users.json");
              }
          }
      }
      return instance;
  }
  ```
- Thread-safe synchronization prevents concurrent file-write corruptions.

### 3.7 Custom Exceptions
- Custom `ValidationException` is thrown when validation rules fail, caught centrally by Javalin exception handlers, and returned as clean HTTP `400 Bad Request` JSON responses `{ "error": "..." }`.

---

## 4. Role-Based Access Control & User Workflows

### 4.1 Administrator Portal (`admin.html`)
- **Access**: Role `ADMIN` (credentials: `admin` / `admin123`).
- **Features**:
  - Top summary cards: Total Workers, Total Users, Active Services, Total Stored Accounts.
  - **Workers Directory Table**: Displays Full Name, Username, Trade Skill, Location, Direct Phone, and **Confidential 12-digit Aadhar**.
  - **Users Directory Table**: Displays all registered customer profiles.
  - Search/filter bars for instantaneous client-side filtering.

### 4.2 Worker Portal (`worker.html`)
- **Access**: Role `WORKER` (credentials: `worker1` / `worker123`).
- **Features**:
  - **Live Profile Preview Card**: Displays active service icon, verified worker status, city location, phone, and Aadhar.
  - **Profile Editor Form**: Workers can update their trade category, mandatory location (plain text, e.g., "Alappuzha"), phone number, and Aadhar.
  - Form validation with real-time Aadhar 12-digit counter.
  - Changes immediately persist to `data/users.json`.

### 4.3 Customer / User Portal (`user.html`)
- **Access**: Role `USER` (credentials: `user1` / `user123`).
- **Features**:
  - **Step 1: Select Service Category**: Interactive cards with icons (Plumbing, Electrical, Carpentry, House Keeping, Painting, Appliance Repair, Pest Control, Gardening).
  - **Step 2: Location Filter**: Text input with popular quick-select city chips (Alappuzha, Kochi, Trivandrum, Kozhikode, Thrissur).
  - **Step 3: Available Worker Cards**: Displays matching workers with 1-click `tel:...` direct call action.
  - **Privacy Guarantee**: Workers' Aadhar numbers are strictly withheld from user search results.

---

## 5. Data Persistence Architecture

Data is stored in `data/users.json` with polymorphic JSON structure:

```json
[
  {
    "role": "ADMIN",
    "username": "admin",
    "password": "admin123",
    "name": "Site Admin",
    "phone": "9800000000"
  },
  {
    "role": "WORKER",
    "username": "worker1",
    "password": "worker123",
    "name": "Ravi Kumar",
    "phone": "9876500002",
    "aadhar": "123412341234",
    "service": "PLUMBING",
    "location": "Alappuzha"
  },
  {
    "role": "USER",
    "username": "user1",
    "password": "user123",
    "name": "Anil Joseph",
    "phone": "9876500001"
  }
]
```

Jackson automatically maps each element to the appropriate subclass (`Admin`, `Worker`, `Customer`) using `@JsonTypeInfo(property = "role")`.

---

## 6. REST API Specification

| Method | Endpoint | Authorization | Description |
|---|---|---|---|
| `POST` | `/api/login` | Public | Authenticates credentials, returns `{ token, role, name, dashboard }` |
| `POST` | `/api/register` | Public | Registers a new USER or WORKER |
| `POST` | `/api/logout` | Bearer Token | Invalidates active session token |
| `GET` | `/api/services` | Public | Returns array of service categories with icon metadata |
| `GET` | `/api/me` | Bearer Token | Returns current authenticated user's profile |
| `GET` | `/api/search` | USER / Public | Query params: `service`, `location`. Returns matching workers (Aadhar hidden) |
| `GET` | `/api/worker/me` | WORKER | Returns worker's own profile including Aadhar |
| `PUT` | `/api/worker/me` | WORKER | Updates worker profile fields and rewrites JSON |
| `GET` | `/api/admin/workers` | ADMIN | Returns all workers with full details (including Aadhar) |
| `GET` | `/api/admin/users` | ADMIN | Returns all registered customer accounts |
| `GET` | `/api/admin/stats` | ADMIN | Returns system analytics and category breakdowns |

---

## 7. Deployment & Run Guide

### 7.1 Running Locally
```bash
# 1. Compile & Package into runnable jar
mvn clean package

# 2. Run application
java -jar target/app.jar

# 3. Access in browser
http://localhost:8080
```

### 7.2 Running with Docker
```bash
docker build -t service-finder .
docker run -p 8080:8080 service-finder
```

### 7.3 Deploying to Render.com
1. Push project to a GitHub repository.
2. On Render.com: **New Web Service** -> Select GitHub repo.
3. Choose **Docker** runtime.
4. Click **Deploy**. Render provides a public HTTPS URL (e.g. `https://service-finder.onrender.com`).

---

## 8. Viva Q&A Preparation Cheat Sheet

**Q1: Why did you use an abstract class `Person` instead of a standard class?**
> *Answer:* `Person` captures the shared attributes (`username`, `password`, `name`, `phone`) and behavior of any user on the platform. Making it abstract prevents instantiating a generic person without a concrete role (`Admin`, `Customer`, or `Worker`), enforcing proper domain modeling.

**Q2: How is Polymorphism used in the login flow?**
> *Answer:* When a user authenticates, the controller calls `person.getDashboardPage()`. The exact page (`admin.html`, `worker.html`, or `user.html`) is determined dynamically at runtime based on the actual subclass object, avoiding hardcoded `if/else` checks.

**Q3: How is data privacy enforced for workers' Aadhar numbers?**
> *Answer:* In `Worker.java`, `toMap(boolean includeSensitive)` controls whether the Aadhar number is serialized into JSON. In `SearchService`, searches for regular users pass `false`, guaranteeing that Aadhar is never exposed on the network or the frontend to customers. Only Admin queries pass `true`.

**Q4: Why is `JsonStore` implemented as a Singleton with `synchronized` methods?**
> *Answer:* Javalin handles HTTP requests concurrently across multiple worker threads. If two users update their profile at the same instant, concurrent file writes could corrupt `users.json`. A Singleton with `synchronized` read/write blocks ensures thread-safe, atomic file I/O.

**Q5: What are the trade-offs of storing data in a JSON file instead of a relational database (SQL)?**
> *Answer:*
> - *Advantages:* Zero database installation/configuration required, completely portable, and easy to inspect/seed for demos and student evaluation.
> - *Limitations in Production:* File-based JSON storage does not scale to thousands of concurrent users, lacks ACID transactions across complex tables, and cannot perform indexed queries. In a real-world enterprise deployment, `Repository<Person>` would be backed by PostgreSQL or MySQL with BCrypt password hashing.
