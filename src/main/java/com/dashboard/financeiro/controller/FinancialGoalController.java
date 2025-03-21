package com.dashboard.financeiro.controller;

import com.dashboard.financeiro.dto.goal.FinancialGoalRequest;
import com.dashboard.financeiro.dto.goal.FinancialGoalResponse;
import com.dashboard.financeiro.model.FinancialGoal;
import com.dashboard.financeiro.service.FinancialGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "*")
public class FinancialGoalController {

    @Autowired
    private FinancialGoalService financialGoalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<FinancialGoalResponse>> getAllGoals(Authentication authentication) {
        List<FinancialGoal> goals = financialGoalService.findAllByUsername(authentication.getName());
        List<FinancialGoalResponse> responses = goals.stream()
                .map(FinancialGoalResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/achieved")
    @PreAuthorize("hasAnyRole('USER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<FinancialGoalResponse>> getAchievedGoals(Authentication authentication) {
        List<FinancialGoal> goals = financialGoalService.findAchievedGoalsByUsername(authentication.getName());
        List<FinancialGoalResponse> responses = goals.stream()
                .map(FinancialGoalResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<FinancialGoalResponse> getGoalById(@PathVariable Long id, Authentication authentication) {
        try {
            FinancialGoal goal = financialGoalService.findById(id, authentication.getName());
            return ResponseEntity.ok(FinancialGoalResponse.fromEntity(goal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FinancialGoalResponse> createGoal(@RequestBody FinancialGoalRequest request, Authentication authentication) {
        try {
            FinancialGoal goal = request.toEntity();
            FinancialGoal savedGoal = financialGoalService.save(goal, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(FinancialGoalResponse.fromEntity(savedGoal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FinancialGoalResponse> updateGoalProgress(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            Authentication authentication) {
        try {
            FinancialGoal updatedGoal = financialGoalService.updateProgress(id, amount, authentication.getName());
            return ResponseEntity.ok(FinancialGoalResponse.fromEntity(updatedGoal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FinancialGoalResponse> updateGoal(
            @PathVariable Long id,
            @RequestBody FinancialGoalRequest request,
            Authentication authentication) {
        try {
            FinancialGoal goal = request.toEntity();
            goal.setId(id);
            FinancialGoal updatedGoal = financialGoalService.save(goal, authentication.getName());
            return ResponseEntity.ok(FinancialGoalResponse.fromEntity(updatedGoal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id, Authentication authentication) {
        try {
            financialGoalService.delete(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
