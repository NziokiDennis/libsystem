package com.example.library_system.repository;

import com.example.library_system.model.BorrowRequest;
import com.example.library_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByUser(User user);
    List<BorrowRequest> findByStatus(BorrowRequest.Status status);
    List<BorrowRequest> findByUserId(Long userId); // ADD THIS
}
