package com.project.service;

import com.project.model.Person;
import com.project.model.ServiceType;
import com.project.model.Worker;
import com.project.repository.JsonStore;
import com.project.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing user search queries for local workers.
 * Ensures data privacy by never returning sensitive data (e.g. Aadhar) to users.
 */
public class SearchService {

    private final Repository<Person> repository;

    public SearchService() {
        this(JsonStore.getInstance());
    }

    public SearchService(Repository<Person> repository) {
        this.repository = repository;
    }

    /**
     * Searches for workers matching the provided service type and/or location.
     * Always hides Aadhar from user search results.
     */
    public List<Map<String, Object>> searchWorkers(String serviceStr, String locationQuery) {
        ServiceType serviceType = null;
        if (serviceStr != null && !serviceStr.trim().isEmpty() && !serviceStr.equalsIgnoreCase("ALL")) {
            try {
                serviceType = ServiceType.fromString(serviceStr);
            } catch (IllegalArgumentException e) {
                // Ignore or treat as no match
            }
        }

        final ServiceType targetService = serviceType;
        final String targetLocation = (locationQuery != null) ? locationQuery.trim().toLowerCase() : "";

        return repository.findAll().stream()
                .filter(p -> p instanceof Worker)
                .map(p -> (Worker) p)
                .filter(w -> {
                    // Match service if provided
                    if (targetService != null && w.getService() != targetService) {
                        return false;
                    }
                    // Match location substring if provided
                    if (!targetLocation.isEmpty()) {
                        String workerLoc = w.getLocation() != null ? w.getLocation().toLowerCase() : "";
                        return workerLoc.contains(targetLocation);
                    }
                    return true;
                })
                .map(w -> w.toMap(false)) // PRIVACY GUARANTEE: Never include Aadhar for public/customer searches
                .collect(Collectors.toList());
    }

    /**
     * Returns list of all available service types with their display names and icon metadata.
     */
    public List<Map<String, String>> getAllServices() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ServiceType st : ServiceType.values()) {
            Map<String, String> item = new HashMap<>();
            item.put("code", st.name());
            item.put("displayName", st.getDisplayName());
            item.put("iconClass", st.getIconClass());
            list.add(item);
        }
        return list;
    }
}
