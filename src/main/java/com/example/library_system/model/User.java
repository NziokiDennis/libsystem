package com.example.library_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Entity mapping to users table
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // Maps to user_id

    @Column(unique = true, nullable = false)
    private String username; // Unique username

    @Column(unique = true, nullable = false)
    private String email; // Unique email

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Hashed password

    @Column(name = "full_name", nullable = false)
    private String fullName; // Full name of user/admin

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // STUDENT or ADMIN

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate; // Creation timestamp

    @Column(name = "is_active", nullable = false)
    private boolean active = true; // Soft delete flag

    // Enum for roles
    public enum Role {
        STUDENT, ADMIN
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}