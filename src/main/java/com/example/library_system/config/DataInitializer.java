package com.example.library_system.config;

import com.example.library_system.model.User;
import com.example.library_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Database Data Initializer
 * 
 * Automatically creates essential system data when the application starts.
 * This includes creating default admin accounts and sample data for development.
 * 
 * Features:
 * - Creates default admin user if none exists
 * - Sets up sample student accounts for testing
 * - Initializes system configuration data
 * - Handles database migration scenarios
 * 
 * Security Note: Default passwords should be changed in production
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    /**
     * Executes initialization logic when application starts
     * This method runs after the application context is fully initialized
     * 
     * @param args Command line arguments passed to the application
     */
    @Override
    public void run(String... args) {
        try {
            System.out.println("=== Initializing Library System Data ===");
            
            // Create default admin account
            createDefaultAdmin();
            
            // Create sample student accounts for testing
            createSampleStudents();
            
            System.out.println("=== Data Initialization Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception here as it would prevent application startup
            // Instead, log the error and continue
        }
    }
    
    /**
     * Creates default admin user if no admin exists
     * This ensures there's always at least one admin account for system access
     */
    private void createDefaultAdmin() {
        try {
            // Check if any admin users already exist
            if (userService.getAllAdmins().isEmpty()) {
                System.out.println("No admin users found. Creating default admin account...");
                
                // Create default admin account
                // IMPORTANT: Change these credentials in production!
                User admin = userService.createAdmin(
                    "admin",                    // username
                    "admin@library.com",        // email  
                    "admin123",                 // password (CHANGE IN PRODUCTION!)
                    "System Administrator"       // full name
                );
                
                System.out.printf("✓ Default admin created: %s (%s)%n", 
                                 admin.getUsername(), admin.getEmail());
                System.out.println("⚠️  SECURITY WARNING: Please change the default admin password!");
                
            } else {
                System.out.println("✓ Admin users already exist. Skipping default admin creation.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Failed to create default admin: " + e.getMessage());
            throw e; // Re-throw to be caught by main run method
        }
    }
    
    /**
     * Creates sample student accounts for development and testing
     * These accounts help developers test the system functionality
     */
    private void createSampleStudents() {
        try {
            // Check if any student users already exist
            if (userService.getAllStudents().isEmpty()) {
                System.out.println("No student users found. Creating sample student accounts...");
                
                // Sample student data for testing
                String[][] sampleStudents = {
                    {"john.doe", "john.doe@student.edu", "student123", "John Doe"},
                    {"jane.smith", "jane.smith@student.edu", "student123", "Jane Smith"},
                    {"bob.wilson", "bob.wilson@student.edu", "student123", "Bob Wilson"}
                };
                
                int createdCount = 0;
                for (String[] studentData : sampleStudents) {
                    try {
                        User student = userService.registerStudent(
                            studentData[0], // username
                            studentData[1], // email
                            studentData[2], // password
                            studentData[3]  // full name
                        );
                        
                        System.out.printf("✓ Sample student created: %s (%s)%n", 
                                         student.getUsername(), student.getFullName());
                        createdCount++;
                        
                    } catch (IllegalArgumentException e) {
                        // Student might already exist, skip
                        System.out.printf("→ Skipping student %s: %s%n", 
                                         studentData[0], e.getMessage());
                    }
                }
                
                if (createdCount > 0) {
                    System.out.printf("✓ Created %d sample student accounts%n", createdCount);
                    System.out.println("📝 Sample login credentials:");
                    System.out.println("   Username: john.doe, Password: student123");
                    System.out.println("   Username: jane.smith, Password: student123");
                    System.out.println("   Username: bob.wilson, Password: student123");
                }
                
            } else {
                System.out.println("✓ Student users already exist. Skipping sample student creation.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Failed to create sample students: " + e.getMessage());
            // Don't re-throw here - sample students are optional
        }
    }
    
    /**
     * Creates additional system configuration data
     * This method can be extended to initialize other system settings
     */
    private void initializeSystemSettings() {
        try {
            // TODO: Initialize system settings like:
            // - Default borrow period (14 days)
            // - Maximum books per student (3 books)
            // - Late fee structure
            // - System maintenance schedules
            
            System.out.println("✓ System settings initialized");
            
        } catch (Exception e) {
            System.err.println("✗ Failed to initialize system settings: " + e.getMessage());
        }
    }
    
    /**
     * Displays important security and configuration information
     * Reminds administrators about security best practices
     */
    private void displaySecurityReminders() {
        System.out.println("\n=== SECURITY REMINDERS ===");
        System.out.println("🔐 Default Admin Credentials:");
        System.out.println("   Username: admin");
        System.out.println("   Password: admin123");
        System.out.println("   ⚠️  CHANGE THESE IMMEDIATELY IN PRODUCTION!");
        
        System.out.println("\n🔒 Security Best Practices:");
        System.out.println("   • Change default passwords");
        System.out.println("   • Enable HTTPS in production");
        System.out.println("   • Configure proper database security");
        System.out.println("   • Set up regular backups");
        System.out.println("   • Monitor system logs");
        
        System.out.println("\n📚 Access URLs:");
        System.out.println("   • Homepage: http://localhost:8080/");
        System.out.println("   • Admin Dashboard: http://localhost:8080/admin/dashboard");
        System.out.println("   • Student Dashboard: http://localhost:8080/student/dashboard");
        System.out.println("===============================\n");
    }
}