// UserRepository.java
package com.example.library_system.repository;

import com.example.library_system.model.User;
import com.example.library_system.model.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRoleAndActive(Role role, boolean active);
}
