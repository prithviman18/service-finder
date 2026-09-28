package com.project.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface defining standard CRUD operations.
 * Demonstrates the Interface OOP concept.
 *
 * @param <T> The entity type managed by this repository
 */
public interface Repository<T> {

    List<T> findAll();

    Optional<T> findByUsername(String username);

    void save(T entity);

    void update(T entity);

    void delete(String username);

    void reload();
}
