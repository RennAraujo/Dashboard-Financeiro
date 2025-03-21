package com.dashboard.financeiro.dto.goal;

import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.FinancialGoal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialGoalRequest {
    private Long id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long categoryId;

    public FinancialGoal toEntity() {
        FinancialGoal goal = new FinancialGoal();
        goal.setId(this.id);
        goal.setName(this.name);
        goal.setDescription(this.description);
        goal.setTargetAmount(this.targetAmount);
        goal.setCurrentAmount(this.currentAmount != null ? this.currentAmount : BigDecimal.ZERO);
        goal.setStartDate(this.startDate != null ? this.startDate : LocalDate.now());
        goal.setEndDate(this.endDate);
        goal.setAchieved(false);
        
        if (this.categoryId != null) {
            Category category = new Category();
            category.setId(this.categoryId);
            goal.setCategory(category);
        }
        
        return goal;
    }
}
