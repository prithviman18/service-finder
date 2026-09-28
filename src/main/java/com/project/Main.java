package com.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.model.Admin;
import com.project.model.Person;
import com.project.model.Worker;
import com.project.service.AdminService;
import com.project.service.AuthService;
import com.project.service.SearchService;
import com.project.service.WorkerService;
import com.project.util.ValidationException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Main application entry point.
 * Configures Javalin server, static files, REST API routing, security checks, and exception handling.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid PORT environment variable '{}', falling back to 8080", portEnv);
            }
        }

        AuthService authService = new AuthService();
        WorkerService workerService = new WorkerService();
        AdminService adminService = new AdminService();
        SearchService searchService = new SearchService();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });
        });

        // Global Exception Handlers
        app.exception(ValidationException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unhandled exception at {}: {}", ctx.path(), e.getMessage(), e);
            String message = e.getMessage() != null ? e.getMessage() : "Internal server error occurred.";
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("error", message));
        });

        // CORS and Common Security Headers
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        app.options("/*", ctx -> ctx.status(HttpStatus.OK));

        // Redirect root to login page
        app.get("/", ctx -> ctx.redirect("/login.html"));

        // ==========================================
        // 1. PUBLIC & AUTHENTICATION ENDPOINTS
        // ==========================================

        // POST /api/login
        app.post("/api/login", ctx -> {
            Map<String, String> body = parseBody(ctx);
            String username = body.get("username");
            String password = body.get("password");
            Map<String, Object> result = authService.login(username, password);
            ctx.json(result);
        });

        // POST /api/register
        app.post("/api/register", ctx -> {
            Map<String, String> body = parseBody(ctx);
            Map<String, Object> result = authService.register(body);
            ctx.status(HttpStatus.CREATED).json(result);
        });

        // POST /api/logout
        app.post("/api/logout", ctx -> {
            String token = extractToken(ctx);
            if (token != null) {
                authService.logout(token);
            }
            ctx.json(Map.of("message", "Logged out successfully"));
        });

        // GET /api/services (List of service categories)
        app.get("/api/services", ctx -> {
            ctx.json(searchService.getAllServices());
        });

        // GET /api/me (Current user session profile)
        app.get("/api/me", ctx -> {
            Person person = requireAuth(ctx, authService);
            boolean isWorkerOrAdmin = (person instanceof Worker) || (person instanceof Admin);
            ctx.json(person.toMap(isWorkerOrAdmin));
        });

        // ==========================================
        // 2. CUSTOMER / USER ENDPOINTS
        // ==========================================

        // GET /api/search?service=...&location=...
        app.get("/api/search", ctx -> {
            String service = ctx.queryParam("service");
            String location = ctx.queryParam("location");
            ctx.json(searchService.searchWorkers(service, location));
        });

        // ==========================================
        // 3. WORKER ENDPOINTS
        // ==========================================

        // GET /api/worker/me
        app.get("/api/worker/me", ctx -> {
            Person person = requireAuth(ctx, authService);
            if (!(person instanceof Worker worker)) {
                ctx.status(HttpStatus.FORBIDDEN).json(Map.of("error", "Access denied. Worker role required."));
                return;
            }
            ctx.json(workerService.getWorkerProfile(worker.getUsername()));
        });

        // PUT /api/worker/me
        app.put("/api/worker/me", ctx -> {
            Person person = requireAuth(ctx, authService);
            if (!(person instanceof Worker worker)) {
                ctx.status(HttpStatus.FORBIDDEN).json(Map.of("error", "Access denied. Worker role required."));
                return;
            }
            Map<String, String> body = parseBody(ctx);
            ctx.json(workerService.updateWorkerProfile(worker.getUsername(), body));
        });

        // ==========================================
        // 4. ADMIN ENDPOINTS
        // ==========================================

        // GET /api/admin/workers
        app.get("/api/admin/workers", ctx -> {
            Person person = requireAuth(ctx, authService);
            if (!(person instanceof Admin)) {
                ctx.status(HttpStatus.FORBIDDEN).json(Map.of("error", "Access denied. Admin role required."));
                return;
            }
            ctx.json(adminService.getAllWorkers());
        });

        // GET /api/admin/users
        app.get("/api/admin/users", ctx -> {
            Person person = requireAuth(ctx, authService);
            if (!(person instanceof Admin)) {
                ctx.status(HttpStatus.FORBIDDEN).json(Map.of("error", "Access denied. Admin role required."));
                return;
            }
            ctx.json(adminService.getAllUsers());
        });

        // GET /api/admin/stats
        app.get("/api/admin/stats", ctx -> {
            Person person = requireAuth(ctx, authService);
            if (!(person instanceof Admin)) {
                ctx.status(HttpStatus.FORBIDDEN).json(Map.of("error", "Access denied. Admin role required."));
                return;
            }
            ctx.json(adminService.getSystemStats());
        });

        app.start(port);
        logger.info("==================================================");
        logger.info("🚀 Local Service Finder Server started on port {}", port);
        logger.info("👉 Access the app at: http://localhost:{}", port);
        logger.info("==================================================");
    }

    private static String extractToken(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }
            return authHeader.trim();
        }
        return ctx.queryParam("token");
    }

    private static Person requireAuth(Context ctx, AuthService authService) {
        String token = extractToken(ctx);
        if (token == null) {
            throw new ValidationException("Authentication required. Please login first.");
        }
        Optional<Person> personOpt = authService.getAuthenticatedPerson(token);
        if (personOpt.isEmpty()) {
            throw new ValidationException("Session expired or invalid token. Please log in again.");
        }
        return personOpt.get();
    }

    private static Map<String, String> parseBody(Context ctx) {
        try {
            String body = ctx.body();
            if (body == null || body.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(body, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new ValidationException("Invalid JSON request body: " + e.getMessage());
        }
    }
}
