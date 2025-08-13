package com.example.library_system.config;

import com.example.library_system.model.User;
import com.example.library_system.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        createDefaultAdmin();
        createSampleStudents();
    }

    private void createDefaultAdmin() {
        if (userService.getAllAdmins().isEmpty()) {
            userService.createAdmin(
                    "admin",
                    "admin@library.com",
                    "admin123",
                    "System Administrator"
            );
        }
    }

    private void createSampleStudents() {
        if (userService.getAllStudents().isEmpty()) {
            String[][] students = {
                    {"john.doe", "john.doe@student.edu", "student123", "John Doe"},
                    {"jane.smith", "jane.smith@student.edu", "student123", "Jane Smith"},
                    {"bob.wilson", "bob.wilson@student.edu", "student123", "Bob Wilson"}
            };
            for (String[] s : students) {
                userService.registerStudent(s[0], s[1], s[2], s[3]);
            }
        }
    }
}
