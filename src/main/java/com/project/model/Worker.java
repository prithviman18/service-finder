package com.project.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.project.util.Validator;

import java.util.Map;

/**
 * Worker role model representing a service provider.
 * Demonstrates:
 * 1. Inheritance (extends Person)
 * 2. Encapsulation (strict validation on aadhar, mandatory location, and service type)
 * 3. Polymorphism (overrides getRole(), getDashboardPage(), toMap())
 */
@JsonTypeName("WORKER")
public class Worker extends Person {

    private String aadhar;
    private String location;
    private ServiceType service;

    public Worker() {
        super();
    }

    public Worker(String username, String password, String name, String phone,
                  String aadhar, String location, ServiceType service) {
        super(username, password, name, phone);
        setAadhar(aadhar);
        setLocation(location);
        setService(service);
    }

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        Validator.validateAadhar(aadhar);
        this.aadhar = aadhar.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        Validator.validateLocation(location);
        this.location = location.trim();
    }

    public ServiceType getService() {
        return service;
    }

    public void setService(ServiceType service) {
        if (service == null) {
            throw new IllegalArgumentException("Service type is mandatory for workers.");
        }
        this.service = service;
    }

    @Override
    public String getRole() {
        return "WORKER";
    }

    @Override
    public String getDashboardPage() {
        return "worker.html";
    }

    @Override
    public Map<String, Object> toMap(boolean includeSensitive) {
        Map<String, Object> map = super.toMap(includeSensitive);
        map.put("service", service != null ? service.name() : null);
        map.put("serviceDisplayName", service != null ? service.getDisplayName() : null);
        map.put("serviceIcon", service != null ? service.getIconClass() : "fa-briefcase");
        map.put("location", location);

        // Security / Privacy: Aadhar is only included if includeSensitive is true (for Admin or Worker's own profile)
        if (includeSensitive) {
            map.put("aadhar", aadhar);
        }
        return map;
    }
}
