package com.project.service;

import com.project.model.Admin;
import com.project.model.Customer;
import com.project.model.Person;
import com.project.model.Worker;
import com.project.repository.JsonStore;
import com.project.repository.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing Administrator queries: viewing all workers, users, and statistics.
 */
public class AdminService {

    private final Repository<Person> repository;

    public AdminService() {
        this(JsonStore.getInstance());
    }

    public AdminService(Repository<Person> repository) {
        this.repository = repository;
    }

    /**
     * Retrieves list of all workers with full details (including Aadhar for admin inspection).
     */
    public List<Map<String, Object>> getAllWorkers() {
        return repository.findAll().stream()
                .filter(p -> p instanceof Worker)
                .map(p -> p.toMap(true)) // Admin can see aadhar
                .collect(Collectors.toList());
    }

    /**
     * Retrieves list of all customers/users.
     */
    public List<Map<String, Object>> getAllUsers() {
        return repository.findAll().stream()
                .filter(p -> p instanceof Customer)
                .map(p -> p.toMap(false))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves aggregated system statistics for dashboard KPI cards.
     */
    public Map<String, Object> getSystemStats() {
        List<Person> all = repository.findAll();
        long workerCount = all.stream().filter(p -> p instanceof Worker).count();
        long customerCount = all.stream().filter(p -> p instanceof Customer).count();
        long adminCount = all.stream().filter(p -> p instanceof Admin).count();

        Map<String, Long> serviceCounts = all.stream()
                .filter(p -> p instanceof Worker)
                .map(p -> (Worker) p)
                .filter(w -> w.getService() != null)
                .collect(Collectors.groupingBy(w -> w.getService().getDisplayName(), Collectors.counting()));

        Map<String, Long> locationCounts = all.stream()
                .filter(p -> p instanceof Worker)
                .map(p -> (Worker) p)
                .filter(w -> w.getLocation() != null && !w.getLocation().trim().isEmpty())
                .collect(Collectors.groupingBy(Worker::getLocation, Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWorkers", workerCount);
        stats.put("totalUsers", customerCount);
        stats.put("totalAdmins", adminCount);
        stats.put("totalAccounts", all.size());
        stats.put("serviceBreakdown", serviceCounts);
        stats.put("locationBreakdown", locationCounts);
        return stats;
    }
}
