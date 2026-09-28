package com.project.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;

/**
 * Admin role model.
 * Demonstrates Inheritance by extending Person.
 */
@JsonTypeName("ADMIN")
public class Admin extends Person {

    public Admin() {
        super();
    }

    public Admin(String username, String password, String name, String phone) {
        super(username, password, name, phone);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getDashboardPage() {
        return "admin.html";
    }

    @Override
    public Map<String, Object> toMap(boolean includeSensitive) {
        return super.toMap(includeSensitive);
    }
}
