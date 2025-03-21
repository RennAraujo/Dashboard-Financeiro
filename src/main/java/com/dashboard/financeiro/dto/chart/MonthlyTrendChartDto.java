package com.dashboard.financeiro.dto.chart;

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
public class MonthlyTrendChartDto {
    
    private List<MonthData> months;
    private BigDecimal maxAmount; // Valor máximo para escala do gráfico
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthData {
        private String month; // Formato "MMM/yyyy" (ex: "Jan/2023")
        private Integer year;
        private Integer monthNumber; // 1-12
        private BigDecimal incomeAmount;
        private BigDecimal expenseAmount;
        private BigDecimal balance;
    }
}
