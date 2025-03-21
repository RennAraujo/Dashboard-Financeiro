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
public class FinancialGoalResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean achieved;
    private CategoryDto category;
    private BigDecimal progressPercentage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name;
        private String type;
    }

    public static FinancialGoalResponse fromEntity(FinancialGoal goal) {
        FinancialGoalResponse response = new FinancialGoalResponse();
        response.setId(goal.getId());
        response.setName(goal.getName());
        response.setDescription(goal.getDescription());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCurrentAmount(goal.getCurrentAmount());
        response.setStartDate(goal.getStartDate());
        response.setEndDate(goal.getEndDate());
        response.setAchieved(goal.isAchieved());

        if (goal.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto(
                goal.getCategory().getId(),
                goal.getCategory().getName(),
                goal.getCategory().getType().name()
            );
            response.setCategory(categoryDto);
        }

        // Calcular a porcentagem de progresso
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = goal.getCurrentAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(goal.getTargetAmount(), 2, BigDecimal.ROUND_HALF_UP);
            response.setProgressPercentage(progress);
        } else {
            response.setProgressPercentage(BigDecimal.ZERO);
        }

        return response;
    }
}
