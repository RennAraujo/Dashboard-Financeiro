package com.dashboard.financeiro.controller.chart;

import com.dashboard.financeiro.dto.chart.CategoryExpenseChartDto;
import com.dashboard.financeiro.dto.chart.ChartDateRangeRequest;
import com.dashboard.financeiro.dto.chart.GoalsProgressChartDto;
import com.dashboard.financeiro.dto.chart.MonthlyTrendChartDto;
import com.dashboard.financeiro.service.chart.ChartDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/charts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChartDataController {

    @Autowired
    private ChartDataService chartDataService;

    /**
     * Endpoint para obter dados formatados para gráfico de distribuição de despesas por categoria
     */
    @GetMapping("/expenses-by-category")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<CategoryExpenseChartDto> getExpensesByCategory(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        CategoryExpenseChartDto chartData = chartDataService.getExpensesByCategory(
                authentication.getName(), startDate, endDate);
        
        return ResponseEntity.ok(chartData);
    }
    
    /**
     * Endpoint POST para obter dados formatados para gráfico de distribuição de despesas por categoria
     */
    @PostMapping("/expenses-by-category")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<CategoryExpenseChartDto> postExpensesByCategory(
            Authentication authentication,
            @RequestBody ChartDateRangeRequest request) {
        
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        
        // Processar o tipo de período se for informado
        if (request.getPeriod() != null) {
            switch (request.getPeriod()) {
                case "monthly":
                    startDate = LocalDate.now().withDayOfMonth(1);
                    endDate = startDate.plusMonths(1).minusDays(1);
                    break;
                case "annual":
                    startDate = LocalDate.now().withDayOfYear(1);
                    endDate = startDate.plusYears(1).minusDays(1);
                    break;
                case "last3months":
                    endDate = LocalDate.now();
                    startDate = endDate.minusMonths(3).withDayOfMonth(1);
                    break;
                case "last6months":
                    endDate = LocalDate.now();
                    startDate = endDate.minusMonths(6).withDayOfMonth(1);
                    break;
            }
        }
        
        CategoryExpenseChartDto chartData = chartDataService.getExpensesByCategory(
                authentication.getName(), startDate, endDate);
        
        return ResponseEntity.ok(chartData);
    }

    /**
     * Endpoint para obter dados formatados para gráfico de tendência de receitas e despesas ao longo dos meses
     */
    @GetMapping("/income-expense-trend")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<MonthlyTrendChartDto> getIncomeExpenseTrend(
            Authentication authentication,
            @RequestParam(required = false) Integer months) {
        
        MonthlyTrendChartDto chartData = chartDataService.getMonthlyTrend(
                authentication.getName(), months);
        
        return ResponseEntity.ok(chartData);
    }
    
    /**
     * Endpoint POST para obter dados formatados para gráfico de tendência de receitas e despesas
     */
    @PostMapping("/income-expense-trend")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<MonthlyTrendChartDto> postIncomeExpenseTrend(
            Authentication authentication,
            @RequestBody ChartDateRangeRequest request) {
        
        Integer months = request.getMonths();
        
        // Processar o tipo de período se for informado
        if (request.getPeriod() != null) {
            switch (request.getPeriod()) {
                case "last3months":
                    months = 3;
                    break;
                case "last6months":
                    months = 6;
                    break;
                case "annual":
                    months = 12;
                    break;
            }
        }
        
        MonthlyTrendChartDto chartData = chartDataService.getMonthlyTrend(
                authentication.getName(), months);
        
        return ResponseEntity.ok(chartData);
    }

    /**
     * Endpoint para obter dados formatados para gráfico de progresso das metas financeiras
     */
    @GetMapping("/goals-progress")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<GoalsProgressChartDto> getGoalsProgress(Authentication authentication) {
        
        GoalsProgressChartDto chartData = chartDataService.getGoalsProgress(
                authentication.getName());
        
        return ResponseEntity.ok(chartData);
    }
    
    /**
     * Endpoint para obter todos os dados de gráficos de uma vez
     * Útil para dashboards que precisam exibir múltiplos gráficos
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<DashboardChartsData> getDashboardChartsData(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer months) {
        
        // Se datas não forem fornecidas, usar mês atual
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }
        // Se meses não for fornecido, usar 12 meses
        if (months == null || months <= 0) {
            months = 12;
        }
        
        // Buscar dados para cada gráfico
        CategoryExpenseChartDto expensesData = chartDataService.getExpensesByCategory(
                authentication.getName(), startDate, endDate);
        
        MonthlyTrendChartDto trendData = chartDataService.getMonthlyTrend(
                authentication.getName(), months);
        
        GoalsProgressChartDto goalsData = chartDataService.getGoalsProgress(
                authentication.getName());
        
        // Consolidar todos os dados em um objeto
        DashboardChartsData dashboardData = new DashboardChartsData(
                expensesData, trendData, goalsData);
        
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Endpoint POST para obter todos os dados de gráficos de uma vez
     */
    @PostMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public ResponseEntity<DashboardChartsData> postDashboardChartsData(
            Authentication authentication,
            @RequestBody ChartDateRangeRequest request) {
        
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        Integer months = request.getMonths();
        
        // Processar o tipo de período se for informado
        if (request.getPeriod() != null) {
            switch (request.getPeriod()) {
                case "monthly":
                    startDate = LocalDate.now().withDayOfMonth(1);
                    endDate = startDate.plusMonths(1).minusDays(1);
                    break;
                case "annual":
                    startDate = LocalDate.now().withDayOfYear(1);
                    endDate = startDate.plusYears(1).minusDays(1);
                    months = 12;
                    break;
                case "last3months":
                    endDate = LocalDate.now();
                    startDate = endDate.minusMonths(3).withDayOfMonth(1);
                    months = 3;
                    break;
                case "last6months":
                    endDate = LocalDate.now();
                    startDate = endDate.minusMonths(6).withDayOfMonth(1);
                    months = 6;
                    break;
            }
        }
        
        // Se datas não forem fornecidas, usar mês atual
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }
        // Se meses não for fornecido, usar 12 meses
        if (months == null || months <= 0) {
            months = 12;
        }
        
        // Buscar dados para cada gráfico
        CategoryExpenseChartDto expensesData = chartDataService.getExpensesByCategory(
                authentication.getName(), startDate, endDate);
        
        MonthlyTrendChartDto trendData = chartDataService.getMonthlyTrend(
                authentication.getName(), months);
        
        GoalsProgressChartDto goalsData = chartDataService.getGoalsProgress(
                authentication.getName());
        
        // Consolidar todos os dados em um objeto
        DashboardChartsData dashboardData = new DashboardChartsData(
                expensesData, trendData, goalsData);
        
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * DTO para retornar todos os dados de gráficos em uma única chamada
     */
    private static class DashboardChartsData {
        private final CategoryExpenseChartDto expensesByCategory;
        private final MonthlyTrendChartDto incomeExpenseTrend;
        private final GoalsProgressChartDto goalsProgress;
        
        public DashboardChartsData(
                CategoryExpenseChartDto expensesByCategory,
                MonthlyTrendChartDto incomeExpenseTrend,
                GoalsProgressChartDto goalsProgress) {
            this.expensesByCategory = expensesByCategory;
            this.incomeExpenseTrend = incomeExpenseTrend;
            this.goalsProgress = goalsProgress;
        }
        
        public CategoryExpenseChartDto getExpensesByCategory() {
            return expensesByCategory;
        }
        
        public MonthlyTrendChartDto getIncomeExpenseTrend() {
            return incomeExpenseTrend;
        }
        
        public GoalsProgressChartDto getGoalsProgress() {
            return goalsProgress;
        }
    }
}
