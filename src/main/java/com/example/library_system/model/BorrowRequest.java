package com.example.library_system.model;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Entity mapping to borrow_requests table
@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId; // Maps to request_id

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Student requesting the book

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // Book being requested

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate; // When request was made

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING; // Request status

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Admin who approved/rejected

    @Column(name = "borrow_date")
    private LocalDate borrowDate; // Set when approved

    @Column(name = "due_date")
    private LocalDate dueDate; // Calculated on approval

    @Column(name = "return_date")
    private LocalDate returnDate; // Set when returned

    // Enum for status
    public enum Status {
        PENDING, APPROVED, REJECTED, RETURNED
    }

    // Getters and Setters
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
}