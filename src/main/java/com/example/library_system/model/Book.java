package com.example.library_system.model;

import jakarta.persistence.*;

// Entity mapping to books table
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId; // Maps to book_id

    @Column(unique = true)
    private String isbn; // Unique ISBN, nullable

    @Column(nullable = false)
    private String title; // Book title

    @Column(nullable = false)
    private String author; // Book author

    @Column(name = "total_copies", nullable = false)
    private int totalCopies; // Total stock

    @Column(name = "available_copies", nullable = false)
    private int availableCopies; // Available for borrowing

    // Getters and Setters
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
}