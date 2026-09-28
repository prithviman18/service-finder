package com.project.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.model.Admin;
import com.project.model.Customer;
import com.project.model.Person;
import com.project.model.ServiceType;
import com.project.model.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe JSON file repository implementation.
 * Demonstrates:
 * 1. Interface Implementation (implements Repository<Person>)
 * 2. Singleton Design Pattern (getInstance())
 * 3. File I/O and JSON serialization/deserialization
 */
public class JsonStore implements Repository<Person> {

    private static final Logger logger = LoggerFactory.getLogger(JsonStore.class);
    private static volatile JsonStore instance;

    private final String filePath;
    private final ObjectMapper mapper;
    private final List<Person> people = new CopyOnWriteArrayList<>();

    private JsonStore(String filePath) {
        this.filePath = filePath;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loadFromFile();
    }

    public static JsonStore getInstance() {
        return getInstance("data/users.json");
    }

    public static JsonStore getInstance(String filePath) {
        if (instance == null) {
            synchronized (JsonStore.class) {
                if (instance == null) {
                    instance = new JsonStore(filePath);
                }
            }
        }
        return instance;
    }

    private synchronized void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.warn("Data file {} does not exist. Creating default seeded dataset.", filePath);
            createDefaultData();
            return;
        }

        try {
            List<Person> loaded = mapper.readValue(file, new TypeReference<List<Person>>() {});
            people.clear();
            if (loaded != null) {
                people.addAll(loaded);
            }
            logger.info("Successfully loaded {} users from {}", people.size(), filePath);
        } catch (IOException e) {
            logger.error("Failed to parse JSON file {}: {}. Seeding default data.", filePath, e.getMessage());
            createDefaultData();
        }
    }

    private synchronized void createDefaultData() {
        people.clear();
        people.add(new Admin("admin", "admin123", "Site Admin", "9800000000"));
        people.add(new Worker("worker1", "worker123", "Ravi Kumar", "9876500002", "123412341234", "Alappuzha", ServiceType.PLUMBING));
        people.add(new Worker("worker2", "worker123", "Deepak Menon", "9876500003", "234523452345", "Kochi", ServiceType.ELECTRICAL));
        people.add(new Worker("worker3", "worker123", "Suresh Nair", "9876500004", "345634563456", "Alappuzha", ServiceType.CARPENTRY));
        people.add(new Worker("worker4", "worker123", "Priya Raj", "9876500005", "456745674567", "Trivandrum", ServiceType.HOUSEKEEPING));
        people.add(new Worker("worker5", "worker123", "Manoj Varma", "9876500006", "567856785678", "Alappuzha", ServiceType.PAINTING));
        people.add(new Worker("worker6", "worker123", "Anand Krishnan", "9876500007", "678967896789", "Kochi", ServiceType.APPLIANCE_REPAIR));
        people.add(new Customer("user1", "user123", "Anil Joseph", "9876500001"));
        people.add(new Customer("user2", "user123", "Sneha George", "9876500008"));
        saveToFile();
    }

    private synchronized void saveToFile() {
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            mapper.writeValue(path.toFile(), people);
            logger.info("Saved {} users to {}", people.size(), filePath);
        } catch (IOException e) {
            logger.error("Error writing data to JSON file {}: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("Failed to persist data: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Person> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(people));
    }

    @Override
    public Optional<Person> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return people.stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst();
    }

    @Override
    public synchronized void save(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Cannot save null person");
        }
        if (findByUsername(person.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + person.getUsername() + "' is already registered.");
        }
        people.add(person);
        saveToFile();
    }

    @Override
    public synchronized void update(Person updatedPerson) {
        if (updatedPerson == null) {
            throw new IllegalArgumentException("Cannot update null person");
        }
        for (int i = 0; i < people.size(); i++) {
            if (people.get(i).getUsername().equalsIgnoreCase(updatedPerson.getUsername())) {
                people.set(i, updatedPerson);
                saveToFile();
                return;
            }
        }
        throw new IllegalArgumentException("User '" + updatedPerson.getUsername() + "' not found to update.");
    }

    @Override
    public synchronized void delete(String username) {
        boolean removed = people.removeIf(p -> p.getUsername().equalsIgnoreCase(username.trim()));
        if (removed) {
            saveToFile();
        }
    }

    @Override
    public synchronized void reload() {
        loadFromFile();
    }
}
