package com.summarizer_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.summarizer_backend.model.SummaryResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.time.LocalDateTime; // 💡 NEW: Crucial import for time-based filtering

public interface SummaryResultRepository extends JpaRepository<SummaryResult, Long> {
    
    // 💡 NEW METHOD: Spring Data JPA automatically generates the query: 
    // SELECT * FROM summary_result WHERE created_date > :createdDate
    List<SummaryResult> findByCreatedDateAfter(LocalDateTime createdDate);
    
    // Existing method to fetch all with user details
    @Query("SELECT s FROM SummaryResult s JOIN FETCH s.user")
    List<SummaryResult> findAllWithUser();
    
    @Transactional
    @Modifying
    @Query("DELETE FROM SummaryResult s WHERE s.user.id = :userId") // Added JPQL for clarity
    void deleteAllByUserId(Long userId);
}