package com.project.service;

import com.project.model.Customer;
import com.project.model.Person;
import com.project.model.ServiceType;
import com.project.model.Worker;
import com.project.repository.JsonStore;
import com.project.repository.Repository;
import com.project.util.ValidationException;
import com.project.util.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing user authentication, registration, and active session tokens.
 */
public class AuthService {

    private final Repository<Person> repository;
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    public AuthService() {
        this(JsonStore.getInstance());
    }

    public AuthService(Repository<Person> repository) {
        this.repository = repository;
    }

    /**
     * Authenticates a user and issues a session token.
     */
    public Map<String, Object> login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new ValidationException("Username and password are required.");
        }

        Optional<Person> personOpt = repository.findByUsername(username.trim());
        if (personOpt.isEmpty() || !personOpt.get().getPassword().equals(password)) {
            throw new ValidationException("Invalid username or password.");
        }

        Person person = personOpt.get();
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, person.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", person.getRole());
        response.put("username", person.getUsername());
        response.put("name", person.getName());
        response.put("dashboard", person.getDashboardPage());
        return response;
    }

    /**
     * Registers a new USER or WORKER.
     */
    public Map<String, Object> register(Map<String, String> data) {
        if (data == null) {
            throw new ValidationException("Registration data cannot be empty.");
        }

        String role = data.get("role");
        String username = data.get("username");
        String password = data.get("password");
        String name = data.get("name");
        String phone = data.get("phone");

        if (role == null || (!role.equalsIgnoreCase("USER") && !role.equalsIgnoreCase("WORKER"))) {
            throw new ValidationException("Role must be either 'USER' or 'WORKER'. Admin accounts are pre-created.");
        }

        Validator.validateUsername(username);
        Validator.validatePassword(password);
        Validator.validateName(name);
        if (phone != null && !phone.trim().isEmpty()) {
            Validator.validatePhone(phone);
        }

        if (repository.findByUsername(username).isPresent()) {
            throw new ValidationException("Username '" + username + "' is already taken. Please choose another.");
        }

        Person newPerson;
        if (role.equalsIgnoreCase("WORKER")) {
            String aadhar = data.get("aadhar");
            String location = data.get("location");
            String serviceStr = data.get("service");

            Validator.validateAadhar(aadhar);
            Validator.validateLocation(location);

            ServiceType service = ServiceType.fromString(serviceStr);
            if (service == null) {
                throw new ValidationException("Valid service type is mandatory for worker registration.");
            }

            newPerson = new Worker(username, password, name, phone, aadhar, location, service);
        } else {
            newPerson = new Customer(username, password, name, phone);
        }

        repository.save(newPerson);

        // Auto-login registered user
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, newPerson.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", newPerson.getRole());
        response.put("username", newPerson.getUsername());
        response.put("name", newPerson.getName());
        response.put("dashboard", newPerson.getDashboardPage());
        response.put("message", "Registration successful!");
        return response;
    }

    /**
     * Terminates an active session token.
     */
    public void logout(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    /**
     * Resolves an authenticated Person by token.
     */
    public Optional<Person> getAuthenticatedPerson(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        String username = activeSessions.get(token.trim());
        if (username == null) {
            return Optional.empty();
        }
        return repository.findByUsername(username);
    }
}
