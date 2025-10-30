package com.summarizer_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.summarizer_backend.model.SummaryResult;
import com.summarizer_backend.repository.SummaryResultRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private SummaryResultRepository summaryResultRepository;

    
    @GetMapping("/history")
    public ResponseEntity<List<SummaryResult>> getAllSummarizationHistory() {
        List<SummaryResult> history = summaryResultRepository.findAll();
        return ResponseEntity.ok(history);
    }
}
