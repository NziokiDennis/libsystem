package com.example.library_system.controller;

import com.example.library_system.service.CustomUserDetailsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Student Controller
 * 
 * Handles all student-specific operations including dashboard access,
 * book browsing, borrow requests, and profile management.
 * 
 * Security: All methods require STUDENT role authentication
 * 
 * Key Features:
 * - Student dashboard with personal information
 * - Book catalog browsing
 * - Borrow request management
 * - Personal borrowing history
 */
@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {
    
    /**
     * Student Dashboard
     * Displays personalized dashboard with borrowed books, due dates, and quick actions
     * 
     * @param authentication Spring Security authentication object
     * @param model Spring MVC model for passing data to view
     * @return student dashboard template name
     */
    @GetMapping("/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        try {
            // Get current student user from authentication
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            model.addAttribute("currentUser", userPrincipal.getUser());
            model.addAttribute("pageTitle", "Student Dashboard");
            model.addAttribute("currentSection", "dashboard");
            
            // TODO: Add borrowed books, due dates, notifications
            // This would require integration with BorrowRequestService
            model.addAttribute("welcomeMessage", 
                "Welcome back, " + userPrincipal.getUser().getFullName() + "!");
            
            return "student/dashboard";
            
        } catch (Exception e) {
            System.err.println("Error loading student dashboard: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to load dashboard");
            return "error/student-error";
        }
    }
    
    /**
     * Browse Books Catalog
     * Displays available books for borrowing with search functionality
     * 
     * @param search Optional search parameter
     * @param model Spring MVC model
     * @return book catalog template
     */
    @GetMapping("/books")
    public String browseBooks(
            @RequestParam(required = false) String search,
            Model model) {
        
        try {
            model.addAttribute("pageTitle", "Book Catalog");
            model.addAttribute("currentSection", "books");
            model.addAttribute("searchQuery", search);
            
            // TODO: Implement book search and display
            // This requires BookService integration
            model.addAttribute("message", "Book catalog functionality coming soon");
            
            return "student/books";
            
        } catch (Exception e) {
            System.err.println("Error loading books: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to load book catalog");
            return "error/student-error";
        }
    }
    
    /**
     * My Borrowed Books
     * Displays current and past borrowing history
     * 
     * @param authentication Spring Security authentication
     * @param model Spring MVC model
     * @return borrowed books template
     */
    @GetMapping("/borrowed")
    public String myBorrowedBooks(Authentication authentication, Model model) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            model.addAttribute("currentUser", userPrincipal.getUser());
            model.addAttribute("pageTitle", "My Borrowed Books");
            model.addAttribute("currentSection", "borrowed");
            
            // TODO: Load user's borrow requests and history
            // This requires BorrowRequestService integration
            
            return "student/borrowed";
            
        } catch (Exception e) {
            System.err.println("Error loading borrowed books: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to load borrowing history");
            return "error/student-error";
        }
    }
    
    /**
     * Student Profile Management
     * View and update personal information
     * 
     * @param authentication Spring Security authentication
     * @param model Spring MVC model
     * @return student profile template
     */
    @GetMapping("/profile")
    public String studentProfile(Authentication authentication, Model model) {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            model.addAttribute("currentUser", userPrincipal.getUser());
            model.addAttribute("pageTitle", "My Profile");
            model.addAttribute("currentSection", "profile");
            
            return "student/profile";
            
        } catch (Exception e) {
            System.err.println("Error loading student profile: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to load profile");
            return "error/student-error";
        }
    }
    
    /**
     * Request Book Borrow
     * Submit a request to borrow a specific book
     * 
     * @param bookId ID of book to borrow
     * @param authentication Spring Security authentication
     * @param model Spring MVC model
     * @return redirect or error page
     */
    @PostMapping("/borrow/{bookId}")
    public String requestBorrow(
            @PathVariable Long bookId,
            Authentication authentication,
            Model model) {
        
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            // TODO: Implement borrow request logic
            // This requires BorrowRequestService integration
            
            model.addAttribute("successMessage", "Borrow request submitted successfully");
            return "redirect:/student/books";
            
        } catch (Exception e) {
            System.err.println("Error processing borrow request: " + e.getMessage());
            model.addAttribute("errorMessage", "Unable to process borrow request");
            return "student/books";
        }
    }
    
    /**
     * Error Handler for Student Section
     * Centralized error handling for student operations
     * 
     * @param e Exception that occurred
     * @param model Spring MVC model
     * @return error template
     */
    @ExceptionHandler(Exception.class)
    public String handleStudentError(Exception e, Model model) {
        System.err.println("Student controller error: " + e.getMessage());
        e.printStackTrace();
        
        model.addAttribute("errorMessage", "An unexpected error occurred");
        model.addAttribute("errorDetails", e.getMessage());
        model.addAttribute("pageTitle", "Error");
        
        return "error/student-error";
    }
}