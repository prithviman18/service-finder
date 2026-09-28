# Local Service Finder: The Friendly Guide

> Read this before you present. If you can explain everything on this page in your own words, you will do great in the viva.

---

## 1. What is this project? (The 30-second version)

Imagine your tap is leaking at 9 PM. You don't know a plumber. You ask a neighbour, who asks another neighbour, and an hour later you still have a leak.

**Local Service Finder** fixes that. It is a website where:

- **Customers (Users)** pick a service (plumbing, electrical, etc.), type their area, and instantly see the workers available there.
- **Workers** (plumbers, electricians, painters...) create a profile with their trade and location so customers can find them.
- **The Admin** is the boss of the website and can see all workers and all users.

That's it. Three types of people, one website.

---

## 2. Who can do what?

| | Admin | Worker | User |
|---|:---:|:---:|:---:|
| Log in | Yes | Yes | Yes |
| See all workers' full details (including Aadhar) | Yes | No | No |
| See all users | Yes | No | No |
| Edit own profile (phone, Aadhar, service, location) | No | Yes | No |
| Search for workers by service + location | No | No | Yes |
| See a worker's Aadhar | Yes | Only their own | **Never** |

After you log in, the page tells you who you are, for example *"Logged in as: WORKER - Ravi Kumar"*. This is how we show that the website recognises your role.

### What each person sees

**Admin page.** Summary boxes at the top (total workers, total users), then two tables: one for workers, one for users. There are search boxes to filter them.

**Worker page.** A profile card showing your current details, and a form to change them. **Location is compulsory** (just type a place name like `Alappuzha`, no maps or coordinates). Aadhar must be exactly 12 digits.

**User page.** Three simple steps:
1. Pick a service (Plumbing, Electrical, Carpentry, House Keeping, Painting, Appliance Repair, Pest Control, Gardening)
2. Type your location
3. See the matching workers, with a Call button next to each

---

## 3. How does it work behind the scenes?

Think of a **restaurant**:

| Restaurant | Our project | What it does |
|---|---|---|
| Customer at the table | **Browser** (HTML/CSS/JS pages) | Shows screens and takes your clicks |
| Waiter | **Main.java** (Javalin routes) | Takes the request from the browser and brings back the answer |
| Chef | **Services** (AuthService, WorkerService, SearchService, AdminService) | Does the real thinking: checks passwords, filters workers |
| Storeroom | **JsonStore** + `users.json` | Where all the ingredients (data) are kept |

The flow when a user searches for a plumber:

```
User clicks "Search"
   -> Browser sends the request (service=PLUMBING, location=Alappuzha)
      -> Main.java (waiter) receives it and passes it on
         -> SearchService (chef) asks JsonStore for all workers
            -> keeps only plumbers in Alappuzha
            -> removes their Aadhar numbers
         <- sends the clean list back
   <- Browser shows worker cards on screen
```

**Important:** there is no separate frontend project and backend project. It is **one folder, one app**. The Java program serves the web pages *and* handles the logic. One command starts everything.

---

## 4. Where is the data stored? (No database!)

We don't use MySQL or any database. All data lives in a simple text file: `data/users.json`.

A tiny piece of it looks like this:

```json
{
  "role": "WORKER",
  "username": "worker1",
  "password": "worker123",
  "name": "Ravi Kumar",
  "phone": "9876500002",
  "aadhar": "123412341234",
  "service": "PLUMBING",
  "location": "Alappuzha"
}
```

Every time someone registers or a worker updates their profile, the Java program changes the data in memory **and** writes it back to this file. When the app restarts, it reads the file again, so nothing is lost.

**Why JSON and not a database?**
Because it needs zero installation, you can open the file and read it, and it is perfect for a small project.

**What's the downside? (Say this honestly in the viva, examiners like honesty.)**
- It won't work well with thousands of users at once.
- You can't do fast searching or complex queries like in SQL.
- Passwords are stored as plain text here. A real website would store *hashed* passwords (for example with BCrypt).

---

## 5. The OOP part (This is what teachers care about most)

OOP means we model real-world things as **classes** and objects. Here's each concept in plain words, with where we used it.

### Abstraction: "Hide the details, show what matters"
`Person` is an **abstract class**, a template for "any human on our site". Everyone has a username, password, name and phone. But you can't create just a "Person". You must be an Admin, Worker or Customer. It's like a blank ID-card template: it's useless until it becomes a real card.

### Inheritance: "Children get things from their parent"
`Admin`, `Customer` and `Worker` all **extend** `Person`, so they automatically get username, password, name and phone. The Worker then adds its own extras: Aadhar, location and service type. We don't rewrite the same code three times.

```
        Person
       /   |   \
   Admin Customer Worker
```

### Polymorphism: "Same instruction, different behaviour"
After login, the code just says `person.getDashboardPage()`. It doesn't care who the person is:
- Admin returns `admin.html`
- Worker returns `worker.html`
- Customer returns `user.html`

Same method name, different result depending on the actual object. No messy `if admin... else if worker...` chains.

### Encapsulation: "Protect your data with rules"
All fields are `private`. You can only change them through setters, and **setters check the rules**:
- `setAadhar()` refuses anything that isn't exactly 12 digits.
- `setLocation()` refuses blank locations (that's how "location is mandatory" is enforced).

Bad data can't sneak in, because the object itself guards the door.

### Interface: "A promise about what you can do"
`Repository<T>` is an interface, a list of promises: `findAll()`, `findByUsername()`, `save()`, `update()`, `delete()`. `JsonStore` keeps those promises using a JSON file. If someone later wants MySQL, they write a new class that keeps the same promises, and the rest of the app doesn't change.

### Singleton: "Only one of me"
`JsonStore` is created **once** and shared everywhere. Why? Many requests can arrive at the same time, and if two of them wrote to the file together it could get corrupted. One shared object with `synchronized` methods makes everyone take turns.

### Custom exceptions: "Clear error messages"
When a rule is broken (like an 11-digit Aadhar), the code throws a `ValidationException`. The server catches it and sends back a friendly message like `{"error": "Aadhar must be 12 digits"}` instead of crashing.

---

## 6. How does login work?

1. You type username and password and press **Login**.
2. The browser sends them to the server (`POST /api/login`).
3. The server finds you in `users.json` and checks the password.
4. If correct, the server makes a random **token** (like a cinema ticket) and remembers "this ticket belongs to worker1".
5. The browser stores the token and opens the right dashboard.
6. Every later request carries the token, so the server knows who is asking and whether they're allowed. For example, a normal user can't open the admin data by typing the URL.
7. **Logout** throws the ticket away.

---

## 7. How does the search work?

The user picks a **service** and types a **location**. The server keeps a worker only if:
- their service **matches** the chosen one, **and**
- their location **contains** what the user typed, ignoring capital letters and extra spaces.

So typing `alappuzha` still finds a worker whose location is `Alappuzha`. Before sending results back, the server **removes the Aadhar number**. Customers should never see it.

---

## 8. The API (a list of doors into the server)

You don't need to memorise these, just understand the pattern: the browser knocks on a door (URL), and the server answers with data.

| Door | Who may use it | What it does |
|---|---|---|
| `POST /api/login` | Anyone | Log in |
| `POST /api/register` | Anyone | Create a User or Worker account |
| `POST /api/logout` | Logged-in | Log out |
| `GET /api/services` | Anyone | List of service types |
| `GET /api/me` | Logged-in | Who am I? |
| `GET /api/search` | User | Find workers (Aadhar hidden) |
| `GET /api/worker/me` | Worker | See my own profile |
| `PUT /api/worker/me` | Worker | Update my profile |
| `GET /api/admin/workers` | Admin | All workers, full details |
| `GET /api/admin/users` | Admin | All users |
| `GET /api/admin/stats` | Admin | Totals and counts |

---

## 9. Project folder tour

```
service-finder/
|-- pom.xml               <- Maven's shopping list (libraries + build settings)
|-- Dockerfile            <- Recipe used to deploy online
|-- data/users.json       <- Our "database"
`-- src/main/
    |-- java/com/project/
    |   |-- Main.java             <- Starts the server, defines the doors (routes)
    |   |-- model/                <- Person, Admin, Customer, Worker, ServiceType
    |   |-- repository/           <- Repository interface + JsonStore
    |   |-- service/              <- The "chefs": Auth, Worker, Admin, Search
    |   `-- util/Validator.java   <- Rules like "Aadhar = 12 digits"
    `-- resources/public/         <- The website itself
        |-- login.html, admin.html, worker.html, user.html
        |-- css/style.css         <- Colours and layout
        `-- js/                   <- Code that talks to the server
```

---

## 10. How to run it

You need **JDK 17** and **Maven** installed.

```bash
mvn clean package         # builds the project into target/app.jar
java -jar target/app.jar  # starts it
```

Open **http://localhost:8080** in your browser.

**Test accounts:**

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Worker | `worker1` | `worker123` |
| User | `user1` | `user123` |

**With Docker (optional):**
```bash
docker build -t service-finder .
docker run -p 8080:8080 service-finder
```

---

## 11. Putting it live on the internet (free)

1. Upload the project to GitHub.
2. Go to **render.com** -> New Web Service -> pick your GitHub repo.
3. Choose **Docker**, choose the **Free** plan, click **Deploy**.
4. You'll get a public link like `https://service-finder.onrender.com`.

**Things to know before the demo:**
- On the free plan, the site **falls asleep** when nobody uses it. The first visit can take up to a minute. **Open the link a few minutes before you present.**
- The free disk is temporary. If the app restarts, `users.json` resets to the original sample data. That's fine, but don't be surprised if your newly registered accounts vanish.
- Free plans change over time, so check Render's website for current terms.

---

## 12. Viva cheat sheet (in simple words)

**Q: Why is `Person` abstract?**
Because "just a person" doesn't exist on our site. Everyone is an Admin, Worker or Customer. `Person` only holds what they share.

**Q: Where did you use polymorphism?**
At login. We call `getDashboardPage()` and each type of person gives its own page, so we don't need if/else for each role.

**Q: How do you protect the Aadhar number?**
Each person object has a `toMap(includeSensitive)` method. For customer searches we pass `false`, so Aadhar is left out before anything leaves the server. Only the admin (and the worker for their own profile) gets it.

**Q: Why is JsonStore a Singleton with `synchronized`?**
Many requests come at once. One shared object where only one thread writes at a time prevents the file from getting corrupted.

**Q: Why JSON instead of a database?**
It's simple, needs no setup, and is easy to demo. But it doesn't scale, has no advanced queries, and offers no transactions. In a real product we'd use MySQL/PostgreSQL through the same `Repository` interface, and hash passwords with BCrypt.

**Q: What would you improve if you had more time?**
- Hash passwords
- Use a real database
- Add worker ratings and reviews
- Let users book a worker, not just find one
- Let sessions expire after some time
- Use proper location/map search

**Q: Where is the frontend and where is the backend?**
Both are in one project. The HTML/CSS/JS files sit in `resources/public`, and the Java server serves them and also provides the API.

---

## 13. A few honest tips for presenting

- **Know the flow, not the code line by line.** If you can explain "login -> token -> dashboard" and "search -> filter -> hide Aadhar", you're in good shape.
- **Demo all three roles.** Log in as a worker, change a location, then log in as a user and search for that same location.
- **Admit the limits.** Saying "plain-text passwords are a demo shortcut, real systems hash them" makes you look more knowledgeable, not less.
- **Try everything once before presenting**, on the live link too.

Good luck!
