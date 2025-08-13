package com.example.library_system.controller;

import com.example.library_system.model.User;
import com.example.library_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin Controller
 * 
 * Handles all admin-specific operations including user management,
 * system administration, and dashboard functionality.
 * 
 * Security: All methods require ADMIN role authentication
 * 
 * Key Features:
 * - Admin dashboard with system statistics
 * - Student user management (create, view, deactivate)
 * - Library system administration
 * - Secure form handling with CSRF protection
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Admin Dashboard
     * Displays system overview, statistics, and quick actions
     * 
     * @param model Spring MVC model for passing data to view
     * @return admin dashboard template name
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            // Get user statistics for dashboard
            UserService.UserStats stats = userService.getUserStats();
            model.addAttribute("userStats", stats);
            
            // Get recent students for quick overview
            model.addAttribute("recentStudents", userService.getAllStudents());
            
            // Add dashboard metadata
            model.addAttribute("pageTitle", "Admin Dashboard");
            model.addAttribute("currentSection", "dashboard");
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            // Log error and show error page
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to load dashboard data");
            return "error/admin-error";
        }
    }
    
    /**
     * Student Management Page
     * Lists all students with management options
     * 
     * @param model Spring MVC model
     * @return student management template
     */
    @GetMapping("/students")
    public String manageStudents(Model model) {
        try {
            model.addAttribute("students", userService.getAllStudents());
            model.addAttribute("pageTitle", "Manage Students");
            model.addAttribute("currentSection", "students");
            
            return "admin/students";
            
        } catch (Exception e) {
            System.err.println("Error loading students: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to load student data");
            return "error/admin-error";
        }
    }
    
    /**
     * Create Student Form Page
     * Displays form for creating new student accounts
     * 
     * @param model Spring MVC model
     * @return create student form template
     */
    @GetMapping("/students/create")
    public String createStudentForm(Model model) {
        model.addAttribute("pageTitle", "Create New Student");
        model.addAttribute("currentSection", "students");
        return "admin/create-student";
    }
    
    /**
     * Process Student Creation
     * Handles form submission for creating new student accounts
     * 
     * @param username Student username
     * @param email Student email
     * @param password Student password
     * @param fullName Student full name
     * @param redirectAttributes For flash messages
     * @return redirect to student management page
     */
    @PostMapping("/students/create")
    public String createStudent(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate input parameters
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            
            // Create the student user
            User newStudent = userService.registerStudent(username, email, password, fullName);
            
            // Success message
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Student account created successfully for %s (%s)", 
                             newStudent.getFullName(), newStudent.getUsername()));
            
            return "redirect:/admin/students";
            
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("fullName", fullName);
            
            return "redirect:/admin/students/create";
            
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("Error creating student: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "An unexpected error occurred while creating the student account");
            
            return "redirect:/admin/students/create";
        }
    }
    
    /**
     * View Student Details
     * Displays detailed information about a specific student
     * 
     * @param userId Student ID to view
     * @param model Spring MVC model
     * @return student details template
     */
    @GetMapping("/students/{userId}")
    public String viewStudent(@PathVariable Long userId, Model model) {
        try {
            // Find student by ID (this would need to be implemented in UserService)
            // For now, we'll get all students and filter
            User student = userService.getAllStudents().stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
            
            model.addAttribute("student", student);
            model.addAttribute("pageTitle", "Student Details: " + student.getFullName());
            model.addAttribute("currentSection", "students");
            
            return "admin/student-details";
            
        } catch (Exception e) {
            System.err.println("Error loading student details: " + e.getMessage());
            model.addAttribute("errorMessage", "Student not found or unable to load details");
            return "redirect:/admin/students";
        }
    }
    
    /**
     * Deactivate Student Account
     * Soft delete - disables student account without removing data
     * 
     * @param userId Student ID to deactivate
     * @param redirectAttributes For flash messages
     * @return redirect to student management page
     */
    @PostMapping("/students/{userId}/deactivate")
    public String deactivateStudent(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            User deactivatedUser = userService.deactivateUser(userId);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Student account for %s has been deactivated", 
                             deactivatedUser.getFullName()));
            
        } catch (Exception e) {
            System.err.println("Error deactivating student: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Unable to deactivate student account");
        }
        
        return "redirect:/admin/students";
    }
    
    /**
     * Reactivate Student Account
     * Re-enables a previously deactivated student account
     * 
     * @param userId Student ID to reactivate
     * @param redirectAttributes For flash messages
     * @return redirect to student management page
     */
    @PostMapping("/students/{userId}/reactivate")
    public String reactivateStudent(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            User reactivatedUser = userService.reactivateUser(userId);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Student account for %s has been reactivated", 
                             reactivatedUser.getFullName()));
            
        } catch (Exception e) {
            System.err.println("Error reactivating student: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Unable to reactivate student account");
        }
        
        return "redirect:/admin/students";
    }
    
    /**
     * System Settings Page
     * Configuration options for the library system
     * 
     * @param model Spring MVC model
     * @return settings template
     */
    @GetMapping("/settings")
    public String systemSettings(Model model) {
        model.addAttribute("pageTitle", "System Settings");
        model.addAttribute("currentSection", "settings");
        
        // Add current system configuration
        // This could be enhanced to load actual configuration values
        model.addAttribute("maxBorrowDays", 14); // Default borrow period
        model.addAttribute("maxBooksPerUser", 3); // Max books per student
        model.addAttribute("systemMaintenanceMode", false);
        
        return "admin/settings";
    }
    
    /**
     * Book Management Page (Future Enhancement)
     * Placeholder for book management functionality
     * 
     * @param model Spring MVC model
     * @return book management template
     */
    @GetMapping("/books")
    public String manageBooks(Model model) {
        model.addAttribute("pageTitle", "Manage Books");
        model.addAttribute("currentSection", "books");
        
        // TODO: Implement book management functionality
        // This would integrate with BookService when implemented
        model.addAttribute("message", "Book management functionality coming soon");
        
        return "admin/books";
    }
    
    /**
     * Reports and Analytics Page
     * System usage statistics and reports
     * 
     * @param model Spring MVC model
     * @return reports template
     */
    @GetMapping("/reports")
    public String viewReports(Model model) {
        try {
            UserService.UserStats stats = userService.getUserStats();
            
            model.addAttribute("pageTitle", "Reports & Analytics");
            model.addAttribute("currentSection", "reports");
            model.addAttribute("userStats", stats);
            
            // TODO: Add more detailed analytics
            // - Book borrowing statistics
            // - Popular books report
            // - User activity metrics
            // - Overdue books report
            
            return "admin/reports";
            
        } catch (Exception e) {
            System.err.println("Error loading reports: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to load report data");
            return "error/admin-error";
        }
    }
    
    /**
     * Admin Profile Page
     * Current admin user's profile information
     * 
     * @param model Spring MVC model
     * @return admin profile template
     */
    @GetMapping("/profile")
    public String adminProfile(Model model) {
        model.addAttribute("pageTitle", "Admin Profile");
        model.addAttribute("currentSection", "profile");
        
        // TODO: Get current admin user from SecurityContext
        // This requires integration with Spring Security's Authentication
        
        return "admin/profile";
    }
    
    /**
     * Logout Handler
     * Handles admin logout with redirect
     * 
     * @return redirect to home page
     */
    @PostMapping("/logout")
    public String logout() {
        // Spring Security handles the actual logout
        // This method mainly serves as a POST endpoint for logout forms
        return "redirect:/?logout=true";
    }
    
    /**
     * Error Handler for Admin Section
     * Centralized error handling for admin operations
     * 
     * @param e Exception that occurred
     * @param model Spring MVC model
     * @return error template
     */
    @ExceptionHandler(Exception.class)
    public String handleAdminError(Exception e, Model model) {
        System.err.println("Admin controller error: " + e.getMessage());
        e.printStackTrace();
        
        model.addAttribute("errorMessage", "An unexpected error occurred");
        model.addAttribute("errorDetails", e.getMessage());
        model.addAttribute("pageTitle", "Error");
        
        return "error/admin-error";
    }
}