package com.dashboard.financeiro.repository;

import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.Transaction;
import com.dashboard.financeiro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUser(User user);
    
    List<Transaction> findByUserAndCategory(User user, Category category);
    
    List<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.category = :category AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserAndCategoryAndDateBetween(
            @Param("user") User user, 
            @Param("category") Category category, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
            
    List<Transaction> findByUserOrderByDateDesc(User user);
}
