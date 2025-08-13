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

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerStudent(String username, String email, String rawPassword, String fullName) {
        validateUserInput(username, email, rawPassword, fullName);
        ensureUsernameUnique(username);

        return saveUser(username, email, rawPassword, fullName, User.Role.STUDENT);
    }

    public User createAdmin(String username, String email, String rawPassword, String fullName) {
        validateUserInput(username, email, rawPassword, fullName);
        ensureUsernameUnique(username);

        return saveUser(username, email, rawPassword, fullName, User.Role.ADMIN);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(username)
                .map(s -> s.trim().toLowerCase())
                .flatMap(userRepository::findByUsername);
    }

    @Transactional(readOnly = true)
    public List<User> getAllStudents() {
        return userRepository.findByRoleAndActive(User.Role.STUDENT, true);
    }

    @Transactional(readOnly = true)
    public List<User> getAllAdmins() {
        return userRepository.findByRoleAndActive(User.Role.ADMIN, true);
    }

    public User updatePassword(Long userId, String newRawPassword) {
        if (newRawPassword == null || newRawPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        User user = getUserById(userId);
        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        return userRepository.save(user);
    }

    public User deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        return userRepository.save(user);
    }

    public User reactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserStats getUserStats() {
        long totalStudents = userRepository.findByRoleAndActive(User.Role.STUDENT, true).size();
        long totalAdmins = userRepository.findByRoleAndActive(User.Role.ADMIN, true).size();
        return new UserStats(totalStudents + totalAdmins, totalStudents, totalAdmins);
    }

    private void validateUserInput(String username, String email, String password, String fullName) {
        if (username == null || username.trim().length() < 3)
            throw new IllegalArgumentException("Invalid username");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email");
        if (password == null || password.trim().length() < 6)
            throw new IllegalArgumentException("Invalid password");
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Full name cannot be empty");
    }

    private void ensureUsernameUnique(String username) {
        if (userRepository.findByUsername(username.trim().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private User saveUser(String username, String email, String rawPassword, String fullName, User.Role role) {
        User user = new User();
        user.setUsername(username.trim().toLowerCase());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName.trim());
        user.setRole(role);
        user.setRegistrationDate(LocalDateTime.now());
        user.setActive(true);
        return userRepository.save(user);
    }

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
