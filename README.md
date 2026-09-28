# Local Service Finder 🛠️

A full-stack, zero-database local service discovery web application built with **Java 17**, **Javalin 6.x**, **Jackson**, and modern **Vanilla HTML5/CSS3/JavaScript**.

---

## 🌟 Key Features

1. **Role-Based Access & Dynamic Dashboards**:
   - **ADMIN**:
     - Complete oversight of all registered service workers and customers.
     - View worker name, service skill, location, phone number, and confidential 12-digit Aadhar number.
     - System analytics (total workers, users, service breakdown).
   - **WORKER**:
     - View and update own profile: Name, Phone (10 digits), Aadhar Number (12 digits), Service Category, and Mandatory Location (e.g. Alappuzha).
     - Changes are immediately persisted to `data/users.json`.
   - **USER (Customer)**:
     - **Step 1**: Interactive Service Category selector (Plumbing, Electrical, Carpentry, House Keeping, Painting, Appliance Repair, Pest Control, Gardening).
     - **Step 2**: Location input with quick city tags (Alappuzha, Kochi, Trivandrum, Kozhikode, Thrissur).
     - **Step 3**: Instant worker results with 1-click direct calling.
     - **Privacy Guarantee**: Workers' Aadhar numbers are verified by Admin and **never** exposed to users.
2. **Zero-Database Storage**:
   - Thread-safe persistence to `data/users.json` using Jackson Databind and Singleton design pattern.
3. **Pure OOP Design**:
   - Abstraction, Inheritance, Polymorphism, Encapsulation, Interfaces, and Enums.

---

## 🏗️ OOP Concepts Breakdown (For Viva / Report)

| OOP Concept | Implementation in Project | Description |
|---|---|---|
| **Abstraction** | `Person` abstract class | Defines common user fields and abstract methods (`getRole()`, `getDashboardPage()`, `toMap()`). Cannot be instantiated directly. |
| **Inheritance** | `Admin`, `Customer`, `Worker` extend `Person` | Inherits common attributes (`username`, `password`, `name`, `phone`) and extends specialized fields (`Worker` adds `aadhar`, `location`, `service`). |
| **Polymorphism** | `person.getDashboardPage()`, `person.toMap()` | Dynamic method dispatch; login router resolves the correct dashboard and privacy filters polymorphically. |
| **Encapsulation** | Private fields + Setters with validation | All model attributes are private. Setters enforce invariants (12-digit Aadhar, 10-digit phone, mandatory non-blank location). |
| **Interface** | `Repository<T>` | Generic repository contract defining CRUD operations (`findAll()`, `findByUsername()`, `save()`, `update()`, `delete()`). |
| **Singleton Pattern** | `JsonStore.getInstance()` | Thread-safe Singleton managing synchronized read/write access to `data/users.json`. |
| **Enum** | `ServiceType` | Strongly typed service categories with display names and FontAwesome icon classes. |
| **Exception Handling** | `ValidationException` | Custom runtime exceptions caught by Javalin global exception handlers and formatted into clean JSON responses. |

---

## 👥 Default Demo Accounts (Pre-Seeded)

| Role | Username | Password | Details |
|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | Site Admin |
| **WORKER** | `worker1` | `worker123` | Ravi Kumar (Plumbing, Alappuzha) |
| **WORKER** | `worker2` | `worker123` | Deepak Menon (Electrical, Kochi) |
| **USER** | `user1` | `user123` | Anil Joseph |

*(Quick-login buttons are also provided on `login.html` for instant demoing)*

---

## 🚀 How to Run Locally

### Prerequisites
- JDK 17+
- Apache Maven 3.9+

### Build & Run
```bash
# 1. Navigate to project root
cd service-finder

# 2. Package into a runnable JAR
mvn clean package

# 3. Start the application
java -jar target/app.jar

# 4. Open in your browser
# http://localhost:8080
```

---

## 🐳 Docker & Cloud Deployment (Render.com)

A production-ready multi-stage `Dockerfile` is included:

```bash
# Build and run Docker container locally
docker build -t service-finder .
docker run -p 8080:8080 service-finder
```

### Deploying to Render:
1. Push this repository to GitHub.
2. Log into [Render.com](https://render.com) -> **New Web Service**.
3. Select your repository and choose **Docker** runtime.
4. Click **Deploy**. Render automatically sets the `PORT` environment variable which `Main.java` dynamically reads.

---

## 📡 REST API Reference

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/login` | Public | Authenticates credentials, returns session token & role |
| `POST` | `/api/register` | Public | Registers a new USER or WORKER |
| `POST` | `/api/logout` | Logged in | Invalidates active session token |
| `GET` | `/api/services` | Public | Returns available service categories |
| `GET` | `/api/search` | USER / Public | Query workers by `service` and `location` (Aadhar hidden) |
| `GET` | `/api/worker/me` | WORKER | Retrieves current worker profile (Aadhar visible) |
| `PUT` | `/api/worker/me` | WORKER | Updates worker profile & persists to JSON |
| `GET` | `/api/admin/workers` | ADMIN | Retrieves all workers with confidential Aadhar |
| `GET` | `/api/admin/users` | ADMIN | Retrieves all registered customer accounts |
| `GET` | `/api/admin/stats` | ADMIN | Aggregated system metrics for admin cards |
