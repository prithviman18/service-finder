package com.project.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.project.util.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class representing a Person in the system.
 * Demonstrates:
 * 1. Abstraction (cannot be instantiated directly; abstract methods defined)
 * 2. Encapsulation (private fields with controlled access and validation in setters)
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "role",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Admin.class, name = "ADMIN"),
    @JsonSubTypes.Type(value = Customer.class, name = "USER"),
    @JsonSubTypes.Type(value = Worker.class, name = "WORKER")
})
public abstract class Person {

    private String username;
    private String password;
    private String name;
    private String phone;

    public Person() {
    }

    public Person(String username, String password, String name, String phone) {
        setUsername(username);
        setPassword(password);
        setName(name);
        setPhone(phone);
    }

    // Abstract methods demonstrating Abstraction & Polymorphism
    public abstract String getRole();
    public abstract String getDashboardPage();

    // Getters and Setters demonstrating Encapsulation
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        Validator.validateUsername(username);
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        Validator.validatePassword(password);
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validator.validateName(name);
        this.name = name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            Validator.validatePhone(phone);
            this.phone = phone.trim();
        } else {
            this.phone = "";
        }
    }

    /**
     * Converts this Person to a Map for JSON serialization.
     * Polymorphic method overridden by subclasses.
     */
    public Map<String, Object> toMap(boolean includeSensitive) {
        Map<String, Object> map = new HashMap<>();
        map.put("role", getRole());
        map.put("username", getUsername());
        map.put("name", getName());
        map.put("phone", getPhone());
        map.put("dashboard", getDashboardPage());
        if (includeSensitive) {
            map.put("password", getPassword());
        }
        return map;
    }

    @Override
    public String toString() {
        return String.format("%s [role=%s, username=%s, name=%s, phone=%s]",
                getClass().getSimpleName(), getRole(), username, name, phone);
    }
}
