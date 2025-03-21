package com.dashboard.financeiro.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryResponse {
    
    private BigDecimal currentBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private List<FinancialGoalDto> achievedGoals;
    private List<CategorySummaryDto> expensesByCategory;
    private List<CategorySummaryDto> incomesByCategory;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummaryDto {
        private Long categoryId;
        private String categoryName;
        private BigDecimal amount;
        private String categoryType;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialGoalDto {
        private Long id;
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private boolean achieved;
        private String category;
    }
}
