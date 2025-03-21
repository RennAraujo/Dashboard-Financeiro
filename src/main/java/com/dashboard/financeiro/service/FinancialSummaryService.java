package com.dashboard.financeiro.service;

import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse.CategorySummaryDto;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse.FinancialGoalDto;
import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.FinancialGoal;
import com.dashboard.financeiro.model.Transaction.TransactionType;
import com.dashboard.financeiro.model.User;
import com.dashboard.financeiro.repository.FinancialGoalRepository;
import com.dashboard.financeiro.repository.TransactionRepository;
import com.dashboard.financeiro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialSummaryService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FinancialGoalRepository financialGoalRepository;
    
    public FinancialSummaryResponse getFinancialSummary(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Se não houver datas definidas, utilize o mês atual
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }
        
        // Calcular somas
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateBetween(
                user, TransactionType.INCOME, startDate, endDate);
        
        BigDecimal totalExpense = transactionRepository.sumByUserAndTypeAndDateBetween(
                user, TransactionType.EXPENSE, startDate, endDate);
        
        // Ajustar valores nulos
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }
        
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }
        
        // Calcular balanço
        BigDecimal currentBalance = totalIncome.subtract(totalExpense);
        
        // Obter metas atingidas
        List<FinancialGoal> achievedGoals = financialGoalRepository.findByUserAndAchievedTrue(user);
        List<FinancialGoalDto> achievedGoalDtos = mapToFinancialGoalDtos(achievedGoals);
        
        // Obter despesas por categoria
        List<CategorySummaryDto> expensesByCategory = getCategorySummaries(
                user, TransactionType.EXPENSE, startDate, endDate);
        
        // Obter receitas por categoria
        List<CategorySummaryDto> incomesByCategory = getCategorySummaries(
                user, TransactionType.INCOME, startDate, endDate);
        
        // Criar e retornar o resumo
        return FinancialSummaryResponse.builder()
                .currentBalance(currentBalance)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .achievedGoals(achievedGoalDtos)
                .expensesByCategory(expensesByCategory)
                .incomesByCategory(incomesByCategory)
                .build();
    }
    
    private List<CategorySummaryDto> getCategorySummaries(
            User user, TransactionType type, LocalDate startDate, LocalDate endDate) {
        
        List<Object[]> categorySummaries = transactionRepository.sumByUserAndTypeAndDateBetweenGroupByCategory(
                user, type, startDate, endDate);
        
        List<CategorySummaryDto> result = new ArrayList<>();
        
        for (Object[] summary : categorySummaries) {
            Long categoryId = (Long) summary[0];
            String categoryName = (String) summary[1];
            BigDecimal amount = (BigDecimal) summary[2];
            
            CategorySummaryDto dto = CategorySummaryDto.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .amount(amount)
                    .categoryType(type.getDescription())
                    .build();
            
            result.add(dto);
        }
        
        return result;
    }
    
    private List<FinancialGoalDto> mapToFinancialGoalDtos(List<FinancialGoal> goals) {
        return goals.stream()
                .map(goal -> FinancialGoalDto.builder()
                        .id(goal.getId())
                        .name(goal.getName())
                        .description(goal.getDescription())
                        .targetAmount(goal.getTargetAmount())
                        .currentAmount(goal.getCurrentAmount())
                        .achieved(goal.isAchieved())
                        .category(goal.getCategory() != null ? goal.getCategory().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
