package com.dashboard.financeiro.repository;

import com.dashboard.financeiro.model.FinancialGoal;
import com.dashboard.financeiro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    
    List<FinancialGoal> findByUser(User user);
    
    List<FinancialGoal> findByUserAndAchievedTrue(User user);
}
