package com.example.library_system.controller;

import com.example.library_system.model.Book;
import com.example.library_system.model.BorrowRequest;
import com.example.library_system.model.User;
import com.example.library_system.service.BookService;
import com.example.library_system.service.BorrowRequestService;
import com.example.library_system.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
 * - Borrow request management (instant borrow, no approval)
 * - Personal borrowing history
 */
@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowRequestService borrowRequestService;

    /**
     * Student Dashboard
     * Shows borrowed books, borrow history, and available books.
     */
    @GetMapping("/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        Long studentId = user.getId();

        model.addAttribute("currentUser", user);
        model.addAttribute("pageTitle", "Student Dashboard");
        model.addAttribute("currentSection", "dashboard");

        List<BorrowRequest> borrowedBooks = borrowRequestService.getActiveBorrowsByUserId(studentId);
        model.addAttribute("borrowedBooks", borrowedBooks);

        List<BorrowRequest> borrowHistory = borrowRequestService.getAllBorrowsByUserId(studentId);
        model.addAttribute("borrowHistory", borrowHistory);

        List<Book> availableBooks = bookService.getAllBooks();
        model.addAttribute("availableBooks", availableBooks);

        List<Long> borrowedBookIds = borrowedBooks.stream()
                .map(br -> br.getBook().getId())
                .collect(Collectors.toList());
        model.addAttribute("borrowedBookIds", borrowedBookIds);

        model.addAttribute("welcomeMessage", "Welcome back, " + user.getFullName() + "!");
        return "student/dashboard";
    }

    /**
     * Browse Books Catalog
     */
    @GetMapping("/books")
    public String browseBooks(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("pageTitle", "Book Catalog");
        model.addAttribute("currentSection", "books");
        model.addAttribute("searchQuery", search);
        model.addAttribute("books", bookService.getAllBooks());
        return "student/books";
    }

    /**
     * My Borrowed Books (current and history)
     */
    @GetMapping("/borrowed")
    public String myBorrowedBooks(Authentication authentication, Model model) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        Long studentId = user.getId();

        model.addAttribute("currentUser", user);
        model.addAttribute("pageTitle", "My Borrowed Books");
        model.addAttribute("currentSection", "borrowed");

        model.addAttribute("borrowedBooks", borrowRequestService.getActiveBorrowsByUserId(studentId));
        model.addAttribute("borrowHistory", borrowRequestService.getAllBorrowsByUserId(studentId));

        return "student/borrowed";
    }

    /**
     * Student Profile
     */
    @GetMapping("/profile")
    public String studentProfile(Authentication authentication, Model model) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        model.addAttribute("currentUser", userPrincipal.getUser());
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("currentSection", "profile");
        return "student/profile";
    }

    /**
     * Request Borrow (Instant Borrow, no approval)
     */
    @PostMapping("/borrow/{bookId}")
    public String requestBorrow(@PathVariable Long bookId, Authentication authentication, Model model) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        Book book = bookService.getBookById(bookId);

        boolean alreadyBorrowed = borrowRequestService.getActiveBorrowsByUserId(user.getId())
                .stream()
                .anyMatch(br -> br.getBook().getId().equals(bookId));

        int remainingCopies = bookService.getRemainingCopies(book);

        if (alreadyBorrowed || remainingCopies <= 0) {
            model.addAttribute("errorMessage", "Cannot borrow this book (already borrowed or none available)");
            return "redirect:/student/dashboard";
        }

        borrowRequestService.createBorrowRequest(user.getId(), bookId);
        return "redirect:/student/dashboard";
    }

    /**
     * Centralized error handler for student operations
     */
    @ExceptionHandler(Exception.class)
    public String handleStudentError(Exception e, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred");
        model.addAttribute("errorDetails", e.getMessage());
        model.addAttribute("pageTitle", "Error");
        return "error/student-error";
    }
}
