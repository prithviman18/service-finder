package com.project.service;

import com.project.model.Person;
import com.project.model.ServiceType;
import com.project.model.Worker;
import com.project.repository.JsonStore;
import com.project.repository.Repository;
import com.project.util.ValidationException;

import java.util.Map;

/**
 * Service managing Worker-specific operations such as profile retrieval and updates.
 */
public class WorkerService {

    private final Repository<Person> repository;

    public WorkerService() {
        this(JsonStore.getInstance());
    }

    public WorkerService(Repository<Person> repository) {
        this.repository = repository;
    }

    /**
     * Retrieves worker profile including private Aadhar number for own view.
     */
    public Map<String, Object> getWorkerProfile(String username) {
        Person person = repository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Worker not found."));

        if (!(person instanceof Worker worker)) {
            throw new ValidationException("User is not a registered worker.");
        }

        return worker.toMap(true); // include sensitive (aadhar) for worker's own view
    }

    /**
     * Updates worker profile details with validation.
     */
    public Map<String, Object> updateWorkerProfile(String username, Map<String, String> data) {
        Person person = repository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Worker not found."));

        if (!(person instanceof Worker worker)) {
            throw new ValidationException("User is not a registered worker.");
        }

        if (data.containsKey("name") && data.get("name") != null) {
            worker.setName(data.get("name"));
        }
        if (data.containsKey("phone") && data.get("phone") != null) {
            worker.setPhone(data.get("phone"));
        }
        if (data.containsKey("aadhar") && data.get("aadhar") != null) {
            worker.setAadhar(data.get("aadhar"));
        }
        if (data.containsKey("location") && data.get("location") != null) {
            worker.setLocation(data.get("location"));
        }
        if (data.containsKey("service") && data.get("service") != null) {
            worker.setService(ServiceType.fromString(data.get("service")));
        }

        repository.update(worker);

        Map<String, Object> response = worker.toMap(true);
        response.put("message", "Profile updated successfully!");
        return response;
    }
}
