# Local Service Finder - Architecture

A complete web app where **Users** find **Workers** (plumbers, electricians, etc.) by service type and location, **Workers** manage their own details, and an **Admin** can view everyone. Frontend is plain HTML/CSS/JS; backend is Java; data lives in a JSON file (no database).

---

## 1. High-Level Architecture

```
 Browser (HTML + CSS + JS)
        |
        |  fetch()  -> JSON over HTTP
        v
 Java Backend (Javalin)              <- also serves the HTML/CSS/JS files
   |-- Controllers  (Main.java routes)
   |-- Services     (AuthService, WorkerService, AdminService, SearchService)
   |-- Repository   (Repository<T>, JsonStore)
        |
        v
   data/users.json
```

One Java app does everything: serves the pages **and** the API.

---

## 2. Project Structure

```
service-finder/
|-- pom.xml
|-- Dockerfile
|-- README.md
|-- architecture.md
|-- data/
|   `-- users.json
`-- src/main/
    |-- java/com/project/
    |   |-- Main.java
    |   |-- model/
    |   |   |-- Person.java           (abstract base class)
    |   |   |-- Admin.java            (extends Person)
    |   |   |-- Customer.java         (extends Person)
    |   |   |-- Worker.java           (extends Person)
    |   |   `-- ServiceType.java      (enum)
    |   |-- repository/
    |   |   |-- Repository.java       (interface)
    |   |   `-- JsonStore.java        (singleton JSON store)
    |   |-- service/
    |   |   |-- AuthService.java      (login, register, sessions)
    |   |   |-- WorkerService.java    (profile updates)
    |   |   |-- AdminService.java     (admin queries & stats)
    |   |   `-- SearchService.java    (privacy-safe worker search)
    |   `-- util/
    |       |-- Validator.java        (aadhar, phone, location checks)
    |       `-- ValidationException.java
    `-- resources/public/
        |-- login.html
        |-- admin.html
        |-- user.html
        |-- worker.html
        |-- css/style.css
        `-- js/
            |-- auth.js
            |-- admin.js
            |-- user.js
            `-- worker.js
```
