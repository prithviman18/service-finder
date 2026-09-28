package com.project.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;

/**
 * Customer (normal User) role model.
 * Demonstrates Inheritance by extending Person.
 */
@JsonTypeName("USER")
public class Customer extends Person {

    public Customer() {
        super();
    }

    public Customer(String username, String password, String name, String phone) {
        super(username, password, name, phone);
    }

    @Override
    public String getRole() {
        return "USER";
    }

    @Override
    public String getDashboardPage() {
        return "user.html";
    }

    @Override
    public Map<String, Object> toMap(boolean includeSensitive) {
        return super.toMap(includeSensitive);
    }
}
