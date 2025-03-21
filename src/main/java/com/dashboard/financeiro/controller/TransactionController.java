package com.dashboard.financeiro.controller;

import com.dashboard.financeiro.dto.transaction.TransactionFilterRequest;
import com.dashboard.financeiro.dto.transaction.TransactionRequest;
import com.dashboard.financeiro.dto.transaction.TransactionResponse;
import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.Transaction;
import com.dashboard.financeiro.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest transactionRequest,
            Authentication authentication) {
        
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setType(transactionRequest.getType());
        transaction.setDate(transactionRequest.getDate());
        transaction.setDescription(transactionRequest.getDescription());
        
        Category category = new Category();
        category.setId(transactionRequest.getCategoryId());
        transaction.setCategory(category);
        
        Transaction savedTransaction = transactionService.save(transaction, authentication.getName());
        
        return new ResponseEntity<>(TransactionResponse.fromEntity(savedTransaction), HttpStatus.CREATED);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(Authentication authentication) {
        List<Transaction> transactions = transactionService.findAllByUser(authentication.getName());
        
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactionResponses);
    }
    
    @GetMapping("/filter")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByFilter(
            @ModelAttribute TransactionFilterRequest filterRequest,
            Authentication authentication) {
        
        List<Transaction> transactions = transactionService.findByFilters(
                authentication.getName(), 
                filterRequest.getCategoryId(), 
                filterRequest.getStartDate(), 
                filterRequest.getEndDate());
        
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactionResponses);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long id,
            Authentication authentication) {
        
        Transaction transaction = transactionService.findById(id, authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest transactionRequest,
            Authentication authentication) {
        
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setType(transactionRequest.getType());
        transaction.setDate(transactionRequest.getDate());
        transaction.setDescription(transactionRequest.getDescription());
        
        Category category = new Category();
        category.setId(transactionRequest.getCategoryId());
        transaction.setCategory(category);
        
        Transaction updatedTransaction = transactionService.update(id, transaction, authentication.getName());
        
        return ResponseEntity.ok(TransactionResponse.fromEntity(updatedTransaction));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        
        transactionService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
