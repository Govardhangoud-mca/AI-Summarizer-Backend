package com.summarizer_backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.summarizer_backend.model.SummaryResult;

public interface SummaryResultRepository extends JpaRepository<SummaryResult, Long> {
}
