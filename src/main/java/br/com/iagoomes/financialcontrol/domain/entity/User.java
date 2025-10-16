package br.com.iagoomes.financialcontrol.domain.entity;

import java.time.LocalDateTime;

/**
 * Domain entity representing a User
 */
public class User {

    private String id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    /**
     * Factory method to create a new User
     */
    public static User create(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Updates user information
     */
    public void updateInfo(String name, String email) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}