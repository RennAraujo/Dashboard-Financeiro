package com.dashboard.financeiro.controller;

import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.service.FinancialSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financial-summary")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FinancialSummaryController {

    @Autowired
    private FinancialSummaryService financialSummaryService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        FinancialSummaryResponse summary = financialSummaryService.getFinancialSummary(
                authentication.getName(), startDate, endDate);
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/monthly")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<FinancialSummaryResponse> getCurrentMonthSummary(Authentication authentication) {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        FinancialSummaryResponse summary = financialSummaryService.getFinancialSummary(
                authentication.getName(), startDate, endDate);
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/annual")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<FinancialSummaryResponse> getCurrentYearSummary(Authentication authentication) {
        LocalDate startDate = LocalDate.now().withDayOfYear(1);
        LocalDate endDate = startDate.plusYears(1).minusDays(1);
        
        FinancialSummaryResponse summary = financialSummaryService.getFinancialSummary(
                authentication.getName(), startDate, endDate);
        
        return ResponseEntity.ok(summary);
    }
}
