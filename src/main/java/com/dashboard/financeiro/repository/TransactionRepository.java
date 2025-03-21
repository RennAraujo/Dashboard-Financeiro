package com.dashboard.financeiro.repository;

import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.Transaction;
import com.dashboard.financeiro.model.Transaction.TransactionType;
import com.dashboard.financeiro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = 'INCOME'")
    BigDecimal sumIncomeByUser(@Param("user") User user);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseByUser(@Param("user") User user);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndTypeAndDateBetween(
            @Param("user") User user, 
            @Param("type") TransactionType type, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t.category.id as categoryId, t.category.name as categoryName, SUM(t.amount) as amount " +
           "FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.id, t.category.name")
    List<Object[]> sumByUserAndTypeAndDateBetweenGroupByCategory(
            @Param("user") User user, 
            @Param("type") TransactionType type, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}
