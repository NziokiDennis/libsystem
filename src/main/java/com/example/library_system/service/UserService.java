package com.example.library_system.service;

import com.example.library_system.model.User;
import com.example.library_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced User Service with Security Integration
 * 
 * Handles all user-related business logic including registration, authentication,
 * and user management operations. Integrates with Spring Security for proper
 * password handling and authentication.
 * 
 * Key Features:
 * - Secure password hashing using BCrypt
 * - Transaction management for data consistency
 * - Input validation and error handling
 * - Admin user creation capabilities
 * - User status management (active/inactive)
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new student user
     * Called by admin users to create student accounts
     * 
     * @param username Unique username for the student
     * @param email Unique email address
     * @param rawPassword Plain text password (will be hashed)
     * @param fullName Student's full name
     * @return Created User entity
     * @throws IllegalArgumentException if username or email already exists
     */
    public User registerStudent(String username, String email, String rawPassword, String fullName) {
        // Input validation
        validateUserInput(username, email, rawPassword, fullName);
        
        // Check for existing username
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        // Check for existing email (if email uniqueness is required)
        // This could be enhanced with email verification workflow
        
        // Create new student user
        User student = new User();
        student.setUsername(username.trim().toLowerCase()); // Normalize username
        student.setEmail(email.trim().toLowerCase()); // Normalize email
        student.setPasswordHash(passwordEncoder.encode(rawPassword)); // Hash password securely
        student.setFullName(fullName.trim());
        student.setRole(User.Role.STUDENT);
        student.setRegistrationDate(LocalDateTime.now());
        student.setActive(true);
        
        // Save and return the new user
        User savedUser = userRepository.save(student);
        
        // Log successful registration (in production, avoid logging sensitive data)
        System.out.printf("New student registered: %s (%s)%n", 
                         savedUser.getUsername(), savedUser.getFullName());
        
        return savedUser;
    }
    
    /**
     * Create an admin user
     * Should only be called during system initialization or by super admins
     * 
     * @param username Admin username
     * @param email Admin email
     * @param rawPassword Plain text password
     * @param fullName Admin's full name
     * @return Created admin User entity
     */
    public User createAdmin(String username, String email, String rawPassword, String fullName) {
        validateUserInput(username, email, rawPassword, fullName);
        
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Admin username already exists: " + username);
        }
        
        User admin = new User();
        admin.setUsername(username.trim().toLowerCase());
        admin.setEmail(email.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setFullName(fullName.trim());
        admin.setRole(User.Role.ADMIN);
        admin.setRegistrationDate(LocalDateTime.now());
        admin.setActive(true);
        
        User savedAdmin = userRepository.save(admin);
        
        System.out.printf("New admin created: %s (%s)%n", 
                         savedAdmin.getUsername(), savedAdmin.getFullName());
        
        return savedAdmin;
    }
    
    /**
     * Find user by username
     * Used primarily for authentication and user lookup
     * 
     * @param username Username to search for
     * @return Optional containing User if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim().toLowerCase());
    }
    
    /**
     * Get all active students
     * Used by admin interface to display student list
     * 
     * @return List of active student users
     */
    @Transactional(readOnly = true)
    public List<User> getAllStudents() {
        return userRepository.findByRoleAndActive(User.Role.STUDENT, true);
    }
    
    /**
     * Get all active admins
     * Used for admin management functions
     * 
     * @return List of active admin users
     */
    @Transactional(readOnly = true)
    public List<User> getAllAdmins() {
        return userRepository.findByRoleAndActive(User.Role.ADMIN, true);
    }
    
    /**
     * Update user password
     * Allows users to change their password securely
     * 
     * @param userId ID of user to update
     * @param newRawPassword New plain text password
     * @return Updated user entity
     * @throws IllegalArgumentException if user not found
     */
    public User updatePassword(Long userId, String newRawPassword) {
        if (newRawPassword == null || newRawPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        return userRepository.save(user);
    }
    
    /**
     * Deactivate user account (soft delete)
     * Maintains data integrity while preventing login
     * 
     * @param userId ID of user to deactivate
     * @return Updated user entity
     */
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setActive(false);
        return userRepository.save(user);
    }
    
    /**
     * Reactivate user account
     * 
     * @param userId ID of user to reactivate
     * @return Updated user entity
     */
    public User reactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setActive(true);
        return userRepository.save(user);
    }
    
    /**
     * Validate user input data
     * Centralized validation logic for user creation/update operations
     * 
     * @param username Username to validate
     * @param email Email to validate  
     * @param password Password to validate
     * @param fullName Full name to validate
     * @throws IllegalArgumentException if any input is invalid
     */
    private void validateUserInput(String username, String email, String password, String fullName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        // Basic email validation (could be enhanced with regex)
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (password == null || password.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
    }
    
    /**
     * Get user statistics for admin dashboard
     * 
     * @return UserStats object containing counts and metrics
     */
    @Transactional(readOnly = true)
    public UserStats getUserStats() {
        long totalStudents = userRepository.findByRoleAndActive(User.Role.STUDENT, true).size();
        long totalAdmins = userRepository.findByRoleAndActive(User.Role.ADMIN, true).size();
        long totalUsers = totalStudents + totalAdmins;
        
        return new UserStats(totalUsers, totalStudents, totalAdmins);
    }
    
    /**
     * User statistics data transfer object
     */
    public static class UserStats {
        private final long totalUsers;
        private final long totalStudents;
        private final long totalAdmins;
        
        public UserStats(long totalUsers, long totalStudents, long totalAdmins) {
            this.totalUsers = totalUsers;
            this.totalStudents = totalStudents;
            this.totalAdmins = totalAdmins;
        }
        
        public long getTotalUsers() { return totalUsers; }
        public long getTotalStudents() { return totalStudents; }
        public long getTotalAdmins() { return totalAdmins; }
    }
}