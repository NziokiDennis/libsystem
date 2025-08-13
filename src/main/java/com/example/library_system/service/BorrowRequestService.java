package com.example.library_system.service;

import com.example.library_system.model.Book;
import com.example.library_system.model.BorrowRequest;
import com.example.library_system.model.User;
import com.example.library_system.repository.BookRepository;
import com.example.library_system.repository.BorrowRequestRepository;
import com.example.library_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BorrowRequestService(BorrowRequestRepository borrowRequestRepository,
                                BookRepository bookRepository,
                                UserRepository userRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a borrow request: immediately BORROWED if copies available.
     * Automatically sets returnDate to 14 days from borrowDate and requestDate to now.
     */
    @Transactional
    public BorrowRequest createBorrowRequest(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        boolean alreadyBorrowed = borrowRequestRepository.findByUser(user)
                .stream()
                .anyMatch(br -> br.getBook().getId().equals(bookId) &&
                                br.getStatus() == BorrowRequest.Status.BORROWED);

        if (alreadyBorrowed) {
            throw new IllegalStateException("User has already borrowed this book");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for this book");
        }

        BorrowRequest request = new BorrowRequest();
        request.setUser(user);
        request.setBook(book);

        LocalDate borrowDate = LocalDate.now();
        request.setBorrowDate(borrowDate);                    // borrow date = today
        request.setDueDate(borrowDate.plusWeeks(2));          // optional due date
        request.setReturnDate(borrowDate.plusDays(14));       // return date = 14 days from borrow
        request.setRequestDate(LocalDateTime.now());          // requestDate = current timestamp
        request.setStatus(BorrowRequest.Status.BORROWED);

        return borrowRequestRepository.save(request);
    }

    /**
     * Get all active borrows (status BORROWED) for a user.
     */
    public List<BorrowRequest> getActiveBorrowsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return borrowRequestRepository.findByUser(user)
                .stream()
                .filter(br -> br.getStatus() == BorrowRequest.Status.BORROWED)
                .collect(Collectors.toList());
    }

    /**
     * Get all borrows for a user (any status).
     */
    public List<BorrowRequest> getAllBorrowsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return borrowRequestRepository.findByUser(user);
    }

    /**
     * Get all currently borrowed books (status BORROWED).
     */
    public List<BorrowRequest> getAllBorrowedBooks() {
        return borrowRequestRepository.findByStatus(BorrowRequest.Status.BORROWED);
    }

    /**
     * Mark a borrow as returned. Sets returnDate to today.
     */
    @Transactional
    public BorrowRequest returnBook(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("BorrowRequest not found: " + requestId));

        if (request.getStatus() != BorrowRequest.Status.BORROWED) {
            throw new IllegalStateException("Cannot return a book that is not borrowed");
        }

        request.setStatus(BorrowRequest.Status.RETURNED);
        request.setReturnDate(LocalDate.now());

        return borrowRequestRepository.save(request);
    }
}
