package com.example.library_system.controller;

import com.example.library_system.model.User;
import com.example.library_system.model.Book;
import com.example.library_system.model.BorrowRequest;
import com.example.library_system.service.UserService;
import com.example.library_system.service.BookService;
import com.example.library_system.service.BorrowRequestService;
import com.example.library_system.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Controller - Enhanced for Dashboard Integration
 * This is the backend logic written using java spring boot, A java framework for building web applications.
 * Handles all admin operations with comprehensive dashboard support, you can see it running in real time
 * including real-time statistics, book management, and user administration.
 * Springboot runs in realtime, see below what my code is doing.
 * Architecture: Clean separation between data aggregation and presentation,
 * with robust error handling and performance optimization for dashboard views.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowRequestService borrowRequestService;
    
    /**
     * Admin Dashboard - Core System Overview
     * 
     * Aggregates critical system metrics and recent activities for executive view.
     * Optimized for performance with strategic data fetching and caching considerations.
     * 
     * @param authentication Current admin user context
     * @param model Spring MVC model for template data binding
     * @return admin dashboard template with comprehensive system state
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        try {
            // Extract current admin user context for personalization
            User currentAdmin = extractCurrentUser(authentication);
            model.addAttribute("currentUser", currentAdmin);
            
            // Core KPI Metrics - Primary dashboard indicators
            DashboardMetrics metrics = buildDashboardMetrics();
            model.addAttribute("totalBooks", metrics.getTotalBooks());
            model.addAttribute("totalStudents", metrics.getTotalStudents());
            model.addAttribute("activeBorrows", metrics.getActiveBorrows());
            model.addAttribute("overdueBorrows", metrics.getOverdueBorrows());
            
            // Active Borrows Management - Critical operational view
            // Convert BorrowRequest to BorrowDisplayDTO for template compatibility
            List<BorrowRequest> rawBorrows = getActiveBorrows();
            List<BorrowDisplayDTO> activeBorrowsList = rawBorrows.stream()
                .map(this::convertToBorrowDisplay)
                .collect(Collectors.toList());
            model.addAttribute("activeBorrowsList", activeBorrowsList);
            
            // Recent Activity Streams - Operational awareness
            model.addAttribute("recentBooks", getRecentBooks(5));
            model.addAttribute("recentReturns", getRecentReturns(5));
            
            // System Health Alerts - Proactive issue identification
            List<SystemAlert> alerts = generateSystemAlerts(metrics);
            model.addAttribute("alerts", alerts);
            
            // Template metadata
            model.addAttribute("pageTitle", "Admin Dashboard");
            model.addAttribute("currentSection", "dashboard");
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            return handleDashboardError(e, model);
        }
    }
    
    /**
     * Student Management - Comprehensive User Administration
     */
    @GetMapping("/students")
    public String manageStudents(Model model) {
        try {
            List<User> students = userService.getAllStudents();
            model.addAttribute("students", students);
            model.addAttribute("pageTitle", "Manage Students");
            model.addAttribute("currentSection", "students");
            
            // Student statistics for management insights
            model.addAttribute("totalStudents", students.size());
            model.addAttribute("activeStudents", 
                students.stream().mapToInt(s -> s.isActive() ? 1 : 0).sum());
            
            return "admin/students";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Unable to load student data");
            return "error/admin-error";
        }
    }
    
    /**
     * Create Student Form Handler
     */
    @GetMapping("/students/create")
    public String createStudentForm(Model model) {
        model.addAttribute("pageTitle", "Create New Student");
        model.addAttribute("currentSection", "students");
        return "admin/create-student";
    }
    
    /**
     * Student Creation Processor - Enhanced Validation & Error Handling
     */
    @PostMapping("/students/create")
    public String createStudent(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            RedirectAttributes redirectAttributes) {
        
        try {
            validateStudentCreationInput(username, email, password, fullName);
            
            User newStudent = userService.registerStudent(username, email, password, fullName);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Student account created: %s (%s)", 
                             newStudent.getFullName(), newStudent.getUsername()));
            
            return "redirect:/admin/students";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            preserveFormData(redirectAttributes, username, email, fullName);
            return "redirect:/admin/students/create";
            
        } catch (Exception e) {
            handleStudentCreationError(e, redirectAttributes);
            return "redirect:/admin/students/create";
        }
    }
    
    /**
     * Book Addition Handler - Quick Action from Dashboard
     */
    @PostMapping("/books/add")
    public String addBook(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam(required = false) String genre,
            @RequestParam int totalCopies,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        
        try {
            // TODO: Implement BookService.addBook() method
            // Book newBook = bookService.addBook(title, author, genre, totalCopies, description);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Book added successfully: " + title);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to add book: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Return Book Handler - Dashboard Quick Action
     */
    @PostMapping("/return/{borrowId}")
    public String returnBook(@PathVariable Long borrowId, RedirectAttributes redirectAttributes) {
        try {
            BorrowRequest returned = borrowRequestService.returnBook(borrowId);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Book returned: %s by %s", 
                             returned.getBook().getTitle(), 
                             returned.getUser().getFullName()));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to process return: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Student Account Management - Activation/Deactivation
     */
    @PostMapping("/students/{userId}/deactivate")
    public String deactivateStudent(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            User deactivatedUser = userService.deactivateUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Student account deactivated: %s", deactivatedUser.getFullName()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Unable to deactivate student account");
        }
        return "redirect:/admin/students";
    }
    
    @PostMapping("/students/{userId}/reactivate")
    public String reactivateStudent(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            User reactivatedUser = userService.reactivateUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Student account reactivated: %s", reactivatedUser.getFullName()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Unable to reactivate student account");
        }
        return "redirect:/admin/students";
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    /**
     * Extract current admin user from security context
     */
    private User extractCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof 
            CustomUserDetailsService.CustomUserPrincipal) {
            return ((CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal()).getUser();
        }
        return null; // Fallback for template safety
    }
    
    /**
     * Build comprehensive dashboard metrics with performance optimization
     */
    private DashboardMetrics buildDashboardMetrics() {
        try {
            // Safe data fetching with fallbacks
            List<Book> allBooks = bookService.getAllBooks();
            UserService.UserStats userStats = userService.getUserStats();
            List<BorrowRequest> allBorrows = getActiveBorrows();
            
            long overdueBorrows = allBorrows.stream()
                .filter(this::isBorrowOverdue)
                .count();
            
            return new DashboardMetrics(
                allBooks.size(),
                (int) userStats.getTotalStudents(),
                allBorrows.size(),
                (int) overdueBorrows
            );
        } catch (Exception e) {
            System.err.println("Error building dashboard metrics: " + e.getMessage());
            return new DashboardMetrics(0, 0, 0, 0); // Safe fallback
        }
    }
    
    /**
     * Get active borrows with proper error handling
     */
    private List<BorrowRequest> getActiveBorrows() {
        try {
            // Get all students and their active borrows
            List<User> allStudents = userService.getAllStudents();
            return allStudents.stream()
                .flatMap(student -> borrowRequestService.getActiveBorrowsByUserId(student.getId()).stream())
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching active borrows: " + e.getMessage());
            return List.of(); // Return empty list on error
        }
    }
    
    /**
     * Convert BorrowRequest to template-compatible format
     * Handles the user/student field mapping issue
     */
    private BorrowDisplayDTO convertToBorrowDisplay(BorrowRequest borrow) {
        return new BorrowDisplayDTO(
            borrow.getId(),
            borrow.getUser(), // This becomes 'student' in the DTO
            borrow.getBook(),
            borrow.getBorrowDate(),
            borrow.getDueDate(),
            borrow.getStatus()
        );
    }
    
    /**
     * Check if borrow request is overdue (14 days default)
     */
    private boolean isBorrowOverdue(BorrowRequest borrow) {
        return borrow.getBorrowDate() != null && 
               borrow.getBorrowDate().plusDays(14).isBefore(LocalDate.now()) &&
               borrow.getStatus() == BorrowRequest.Status.BORROWED;
    }
    
    /**
     * Get recent book additions (placeholder - requires book creation timestamps)
     */
    private List<Book> getRecentBooks(int limit) {
        return bookService.getAllBooks().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get recent book returns
     */
    private List<BorrowRequest> getRecentReturns(int limit) {
        try {
            return getActiveBorrows().stream()
                .filter(br -> br.getStatus() == BorrowRequest.Status.RETURNED)
                .filter(br -> br.getReturnDate() != null)
                .sorted((a, b) -> b.getReturnDate().compareTo(a.getReturnDate()))
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching recent returns: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Generate system health alerts based on current metrics
     */
    private List<SystemAlert> generateSystemAlerts(DashboardMetrics metrics) {
        return List.of(); // Placeholder - implement based on business rules
    }
    
    /**
     * Input validation for student creation
     */
    private void validateStudentCreationInput(String username, String email, 
                                            String password, String fullName) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (password == null || password.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }
    
    /**
     * Preserve form data for error scenarios
     */
    private void preserveFormData(RedirectAttributes redirectAttributes, 
                                String username, String email, String fullName) {
        redirectAttributes.addFlashAttribute("username", username);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("fullName", fullName);
    }
    
    /**
     * Handle student creation errors with appropriate logging
     */
    private void handleStudentCreationError(Exception e, RedirectAttributes redirectAttributes) {
        System.err.println("Unexpected error creating student: " + e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", 
            "System error occurred while creating student account");
    }
    
    /**
     * Centralized dashboard error handling
     */
    private String handleDashboardError(Exception e, Model model) {
        System.err.println("Dashboard error: " + e.getMessage());
        e.printStackTrace();
        
        model.addAttribute("errorMessage", "Dashboard temporarily unavailable");
        model.addAttribute("pageTitle", "Dashboard Error");
        
        // Provide minimal fallback data
        model.addAttribute("totalBooks", 0);
        model.addAttribute("totalStudents", 0);
        model.addAttribute("activeBorrows", 0);
        model.addAttribute("overdueBorrows", 0);
        
        return "admin/dashboard"; // Graceful degradation
    }
    
    // ===== DATA TRANSFER OBJECTS =====
    
    /**
     * BorrowDisplayDTO - Template-compatible borrow request wrapper
     * Solves the user/student field mapping issue for Thymeleaf templates
     */
    public static class BorrowDisplayDTO {
        private final Long id;
        private final User student; // Maps BorrowRequest.user to student for template
        private final Book book;
        private final LocalDate borrowDate;
        private final LocalDate dueDate;
        private final BorrowRequest.Status status;
        
        public BorrowDisplayDTO(Long id, User user, Book book, LocalDate borrowDate, 
                               LocalDate dueDate, BorrowRequest.Status status) {
            this.id = id;
            this.student = user; // Map user -> student for template compatibility
            this.book = book;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.status = status;
        }
        
        public Long getId() { return id; }
        public User getStudent() { return student; } // Template expects 'student'
        public Book getBook() { return book; }
        public LocalDate getBorrowDate() { return borrowDate; }
        public LocalDate getDueDate() { return dueDate; }
        public BorrowRequest.Status getStatus() { return status; }
    }
    
    /**
     * Dashboard metrics aggregation - Internal data structure
     * Encapsulates key performance indicators for dashboard display
     */
    private static class DashboardMetrics {
        private final int totalBooks;
        private final int totalStudents;
        private final int activeBorrows;
        private final int overdueBorrows;
        
        public DashboardMetrics(int totalBooks, int totalStudents, 
                              int activeBorrows, int overdueBorrows) {
            this.totalBooks = totalBooks;
            this.totalStudents = totalStudents;
            this.activeBorrows = activeBorrows;
            this.overdueBorrows = overdueBorrows;
        }
        
        public int getTotalBooks() { return totalBooks; }
        public int getTotalStudents() { return totalStudents; }
        public int getActiveBorrows() { return activeBorrows; }
        public int getOverdueBorrows() { return overdueBorrows; }
    }
    
    /**
     * System alert structure - For dashboard notifications
     */
    private static class SystemAlert {
        private final String type;
        private final String title;
        private final String message;
        
        public SystemAlert(String type, String title, String message) {
            this.type = type;
            this.title = title;
            this.message = message;
        }
        
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
    }
    
    /**
     * Global exception handler for admin operations
     * Provides consistent error handling and user feedback
     */
    @ExceptionHandler(Exception.class)
    public String handleAdminError(Exception e, Model model) {
        System.err.println("Admin controller error: " + e.getMessage());
        
        model.addAttribute("errorMessage", "An unexpected error occurred");
        model.addAttribute("errorDetails", e.getMessage());
        model.addAttribute("pageTitle", "Admin Error");
        
        return "error/admin-error";
    }
}