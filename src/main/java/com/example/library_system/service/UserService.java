package com.example.library_system.service;

import com.example.library_system.model.User;
import com.example.library_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Service class for user-related business logic
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // Register a new student (called by admin)
    public User registerStudent(String username, String email, String password, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password); // Store plain text password
        user.setFullName(fullName);
        user.setRole(User.Role.STUDENT);
        user.setRegistrationDate(LocalDateTime.now());
        user.setActive(true);
        return userRepository.save(user);
    }

    // Authenticate user (for login)
    public Optional<User> authenticate(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && password.equals(user.get().getPasswordHash())) {
            return user;
        }
        return Optional.empty();
    }

    // Get all students (for admin panel)
    public List<User> getAllStudents() {
        return userRepository.findByRoleAndActive(User.Role.STUDENT, true);
    }
}
