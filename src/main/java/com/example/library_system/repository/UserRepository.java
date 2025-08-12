package com.example.library_system.repository;

import com.example.library_system.model.User;
import com.example.library_system.model.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repository interface for User entity
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query method to find user by username
    Optional<User> findByUsername(String username);

    // Custom query method to find active users by role
    // Useful for listing students or admins
    List<User> findByRoleAndActive(Role role, boolean active);
}
