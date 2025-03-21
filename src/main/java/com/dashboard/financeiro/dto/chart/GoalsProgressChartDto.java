package com.dashboard.financeiro.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalsProgressChartDto {
    
    private List<GoalData> goals;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalData {
        private Long goalId;
        private String goalName;
        private String description;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private BigDecimal progressPercentage;
        private Boolean achieved;
        private LocalDate startDate;
        private LocalDate endDate;
        private Long categoryId;
        private String categoryName;
        private Long daysRemaining;
        private String color; // Código de cor hexadecimal para o gráfico
    }
}
