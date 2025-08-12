package com.example.library_system.repository;

import com.example.library_system.model.BorrowRequest;
import com.example.library_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository interface for BorrowRequest entity
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    // Find all requests by user (for user dashboard)
    List<BorrowRequest> findByUser(User user);

    // Find pending requests (for admin panel)
    List<BorrowRequest> findByStatus(BorrowRequest.Status status);
}