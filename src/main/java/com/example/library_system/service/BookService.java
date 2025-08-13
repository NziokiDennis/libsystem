package com.example.library_system.service;

import com.example.library_system.model.Book;
import com.example.library_system.repository.BookRepository;
import com.example.library_system.repository.BorrowRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    public BookService(BookRepository bookRepository, BorrowRequestRepository borrowRequestRepository) {
        this.bookRepository = bookRepository;
        this.borrowRequestRepository = borrowRequestRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
    }

    /**
     * Computes remaining copies for a book by subtracting active borrows.
     */
    public int getRemainingCopies(Book book) {
        long activeBorrows = borrowRequestRepository
                .findAll()
                .stream()
                .filter(br -> br.getBook().getId().equals(book.getId()))
                .filter(br -> br.getStatus() == com.example.library_system.model.BorrowRequest.Status.BORROWED)
                .count();
        return Math.max(book.getTotalCopies() - (int) activeBorrows, 0);
    }

    public boolean canBorrow(Book book, Long userId) {
        // Ensure user has not borrowed same book and it is available
        return getRemainingCopies(book) > 0 &&
               borrowRequestRepository.findByUserId(userId)
                    .stream()
                    .noneMatch(br -> br.getBook().getId().equals(book.getId()) &&
                                     br.getStatus() == com.example.library_system.model.BorrowRequest.Status.BORROWED);
    }
}
