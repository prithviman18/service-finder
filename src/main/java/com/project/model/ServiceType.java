package com.project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Enum representing available service categories in the platform.
 * Demonstrates the Enum OOP concept with custom methods and attributes.
 */
public enum ServiceType {
    PLUMBING("Plumbing", "fa-faucet-drip"),
    ELECTRICAL("Electrical", "fa-bolt"),
    HOUSEKEEPING("House Keeping", "fa-broom"),
    CARPENTRY("Carpentry", "fa-hammer"),
    PAINTING("Painting", "fa-paint-roller"),
    APPLIANCE_REPAIR("Appliance Repair", "fa-wrench"),
    PEST_CONTROL("Pest Control", "fa-shield-virus"),
    GARDENING("Gardening", "fa-seedling");

    private final String displayName;
    private final String iconClass;

    ServiceType(String displayName, String iconClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }

    @JsonValue
    public String getCode() {
        return name();
    }

    @JsonCreator
    public static ServiceType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (ServiceType st : ServiceType.values()) {
            if (st.name().equalsIgnoreCase(clean) || st.displayName.equalsIgnoreCase(value.trim())) {
                return st;
            }
        }
        throw new IllegalArgumentException("Unknown service type: " + value + ". Valid types: " +
                Arrays.toString(ServiceType.values()));
    }
}
