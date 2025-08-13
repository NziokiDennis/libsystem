package com.example.library_system.repository;

import com.example.library_system.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for Book Entity
 * 
 * Provides database access methods for book-related operations.
 * Extends JpaRepository for standard CRUD operations and defines
 * custom query methods for specific business requirements.
 * 
 * Custom Query Methods:
 * - Search books by title, author, or ISBN
 * - Find available books for borrowing
 * - Get books by availability status
 * - Advanced search capabilities
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * Find book by unique ISBN
     * Used for book identification and duplicate prevention
     * 
     * @param isbn Book's ISBN
     * @return Optional containing book if found
     */
    Optional<Book> findByIsbn(String isbn);
    
    /**
     * Find books by exact title match (case-insensitive)
     * 
     * @param title Book title to search for
     * @return List of books matching the title
     */
    List<Book> findByTitleIgnoreCase(String title);
    
    /**
     * Find books by exact author match (case-insensitive)
     * 
     * @param author Author name to search for
     * @return List of books by the specified author
     */
    List<Book> findByAuthorIgnoreCase(String author);
    
    /**
     * Find books with available copies for borrowing
     * Only returns books that have availableCopies > 0
     * 
     * @return List of books available for borrowing
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();
    
    /**
     * Search books by title containing keyword (case-insensitive)
     * Useful for partial title searches
     * 
     * @param keyword Partial title to search for
     * @return List of books with titles containing the keyword
     */
    List<Book> findByTitleContainingIgnoreCase(String keyword);
    
    /**
     * Search books by author containing keyword (case-insensitive)
     * Useful for partial author name searches
     * 
     * @param keyword Partial author name to search for
     * @return List of books by authors containing the keyword
     */
    List<Book> findByAuthorContainingIgnoreCase(String keyword);
    
    /**
     * Advanced search across title and author fields
     * Searches for books where either title or author contains the keyword
     * 
     * @param keyword Search term to look for in title or author
     * @return List of books matching the search criteria
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchByTitleOrAuthor(@Param("keyword") String keyword);
    
    /**
     * Find books with specific availability status
     * 
     * @param minAvailable Minimum number of available copies
     * @return List of books with at least the specified available copies
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies >= :minAvailable")
    List<Book> findBooksWithMinimumAvailability(@Param("minAvailable") int minAvailable);
    
    /**
     * Find books that are currently out of stock
     * Useful for generating restock reports
     * 
     * @return List of books with no available copies
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies = 0")
    List<Book> findOutOfStockBooks();
    
    /**
     * Find books with low stock (configurable threshold)
     * Useful for inventory management and restocking alerts
     * 
     * @param threshold Maximum available copies to consider as low stock
     * @return List of books with available copies <= threshold
     */
    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.availableCopies <= :threshold")
    List<Book> findLowStockBooks(@Param("threshold") int threshold);
    
    /**
     * Get total count of all copies across all books
     * Useful for library statistics and reporting
     * 
     * @return Sum of all totalCopies across all books
     */
    @Query("SELECT SUM(b.totalCopies) FROM Book b")
    Long getTotalBooksCount();
    
    /**
     * Get count of all available copies across all books
     * Useful for availability statistics
     * 
     * @return Sum of all availableCopies across all books
     */
    @Query("SELECT SUM(b.availableCopies) FROM Book b")
    Long getAvailableBooksCount();
    
    /**
     * Find most popular books by calculating borrowed copies
     * Books with fewer available copies relative to total are considered more popular
     * 
     * @return List of books ordered by popularity (most borrowed first)
     */
    @Query("SELECT b FROM Book b WHERE b.totalCopies > 0 " +
           "ORDER BY (b.totalCopies - b.availableCopies) DESC")
    List<Book> findMostPopularBooks();
    
    /**
     * Complex search method combining multiple criteria
     * Searches across title, author, and ISBN fields
     * 
     * @param searchTerm General search term
     * @param minAvailable Minimum available copies (optional)
     * @return List of books matching the comprehensive search criteria
     */
    @Query("SELECT DISTINCT b FROM Book b WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:minAvailable IS NULL OR b.availableCopies >= :minAvailable) " +
           "ORDER BY b.title")
    List<Book> findBooksWithCriteria(@Param("searchTerm") String searchTerm, 
                                    @Param("minAvailable") Integer minAvailable);
}