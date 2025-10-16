package br.com.iagoomes.financialcontrol.domain;

import br.com.iagoomes.financialcontrol.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Provider interface for User persistence operations
 * To be implemented in the infrastructure layer
 */
public interface UserProvider {

    /**
     * Saves a user
     */
    User save(User user);

    /**
     * Finds a user by ID
     */
    Optional<User> findById(String id);

    /**
     * Finds a user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by ID
     */
    boolean existsById(String id);

    /**
     * Checks if an email is already registered
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by ID
     */
    void deleteById(String id);

    /**
     * Lists all users with pagination
     */
    Page<User> findAll(Pageable pageable);
}