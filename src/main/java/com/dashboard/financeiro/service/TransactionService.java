package com.dashboard.financeiro.service;

import com.dashboard.financeiro.model.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    
    Transaction save(Transaction transaction, String username);
    
    List<Transaction> findAllByUser(String username);
    
    List<Transaction> findByFilters(String username, Long categoryId, LocalDate startDate, LocalDate endDate);
    
    Transaction update(Long id, Transaction transaction, String username);
    
    void delete(Long id, String username);
    
    Transaction findById(Long id, String username);
}
