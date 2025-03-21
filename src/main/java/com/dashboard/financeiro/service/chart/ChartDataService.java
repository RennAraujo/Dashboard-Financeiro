package com.dashboard.financeiro.service.chart;

import com.dashboard.financeiro.dto.chart.CategoryExpenseChartDto;
import com.dashboard.financeiro.dto.chart.GoalsProgressChartDto;
import com.dashboard.financeiro.dto.chart.MonthlyTrendChartDto;
import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.FinancialGoal;
import com.dashboard.financeiro.model.Transaction;
import com.dashboard.financeiro.model.Transaction.TransactionType;
import com.dashboard.financeiro.model.User;
import com.dashboard.financeiro.repository.FinancialGoalRepository;
import com.dashboard.financeiro.repository.TransactionRepository;
import com.dashboard.financeiro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartDataService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialGoalRepository financialGoalRepository;

    private static final String[] CHART_COLORS = {
            "#4e73df", "#1cc88a", "#36b9cc", "#f6c23e", "#e74a3b", "#858796",
            "#5a5c69", "#6610f2", "#6f42c1", "#e83e8c", "#fd7e14", "#20c9a6"
    };

    /**
     * Gera dados formatados para gráfico de distribuição de despesas por categoria
     */
    public CategoryExpenseChartDto getExpensesByCategory(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Configurar datas padrão se não fornecidas
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        // Buscar transações de despesa do usuário
        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());

        // Agrupar despesas por categoria
        Map<Category, BigDecimal> expensesByCategory = new HashMap<>();
        for (Transaction transaction : transactions) {
            Category category = transaction.getCategory();
            if (category == null) {
                continue;
            }
            
            BigDecimal amount = expensesByCategory.getOrDefault(category, BigDecimal.ZERO);
            amount = amount.add(transaction.getAmount());
            expensesByCategory.put(category, amount);
        }

        // Calcular total de despesas
        BigDecimal totalExpenses = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Preparar dados para o gráfico
        List<CategoryExpenseChartDto.CategoryData> categoriesData = new ArrayList<>();
        int colorIndex = 0;

        for (Map.Entry<Category, BigDecimal> entry : expensesByCategory.entrySet()) {
            Category category = entry.getKey();
            BigDecimal amount = entry.getValue();
            
            // Calcular percentual da categoria
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.multiply(new BigDecimal("100"))
                        .divide(totalExpenses, 2, RoundingMode.HALF_UP);
            }
            
            // Selecionar cor para o gráfico
            String color = CHART_COLORS[colorIndex % CHART_COLORS.length];
            colorIndex++;
            
            CategoryExpenseChartDto.CategoryData categoryData = CategoryExpenseChartDto.CategoryData.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .amount(amount)
                    .percentage(percentage)
                    .color(color)
                    .build();
            
            categoriesData.add(categoryData);
        }

        // Ordenar por valor decrescente
        categoriesData.sort((c1, c2) -> c2.getAmount().compareTo(c1.getAmount()));

        return CategoryExpenseChartDto.builder()
                .categories(categoriesData)
                .totalAmount(totalExpenses)
                .build();
    }

    /**
     * Gera dados formatados para gráfico de tendência de receitas e despesas ao longo dos meses
     */
    public MonthlyTrendChartDto getMonthlyTrend(String username, Integer months) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Definir período
        if (months == null || months <= 0) {
            months = 12; // Padrão: últimos 12 meses
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        // Buscar todas as transações do período
        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        // Preparar dados por mês
        List<MonthlyTrendChartDto.MonthData> monthsData = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM/yyyy", new Locale("pt", "BR"));
        BigDecimal maxAmount = BigDecimal.ZERO;

        // Criar uma lista de todos os meses no período
        List<YearMonth> yearMonths = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);
        
        while (!current.isAfter(end)) {
            yearMonths.add(current);
            current = current.plusMonths(1);
        }

        // Para cada mês, calcular receitas e despesas
        for (YearMonth yearMonth : yearMonths) {
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            // Filtrar transações do mês
            List<Transaction> monthTransactions = transactions.stream()
                    .filter(t -> !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd))
                    .collect(Collectors.toList());
            
            // Calcular receitas
            BigDecimal income = monthTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calcular despesas
            BigDecimal expense = monthTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calcular saldo
            BigDecimal balance = income.subtract(expense);
            
            // Atualizar valor máximo para escala do gráfico
            if (income.compareTo(maxAmount) > 0) {
                maxAmount = income;
            }
            if (expense.compareTo(maxAmount) > 0) {
                maxAmount = expense;
            }
            
            // Criar dados do mês
            MonthlyTrendChartDto.MonthData monthData = MonthlyTrendChartDto.MonthData.builder()
                    .month(yearMonth.format(monthFormatter))
                    .year(yearMonth.getYear())
                    .monthNumber(yearMonth.getMonthValue())
                    .incomeAmount(income)
                    .expenseAmount(expense)
                    .balance(balance)
                    .build();
            
            monthsData.add(monthData);
        }

        return MonthlyTrendChartDto.builder()
                .months(monthsData)
                .maxAmount(maxAmount.multiply(new BigDecimal("1.1"))) // Adicionar 10% de margem
                .build();
    }

    /**
     * Gera dados formatados para gráfico de progresso das metas financeiras
     */
    public GoalsProgressChartDto getGoalsProgress(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Buscar todas as metas do usuário
        List<FinancialGoal> goals = financialGoalRepository.findByUser(user);
        
        // Preparar dados de metas
        List<GoalsProgressChartDto.GoalData> goalsData = new ArrayList<>();
        int colorIndex = 0;
        LocalDate today = LocalDate.now();
        
        for (FinancialGoal goal : goals) {
            // Calcular porcentagem de progresso
            BigDecimal progressPercentage = BigDecimal.ZERO;
            if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                progressPercentage = goal.getCurrentAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
            }
            
            // Calcular dias restantes
            Long daysRemaining = null;
            if (goal.getEndDate() != null && !goal.isAchieved() && goal.getEndDate().isAfter(today)) {
                daysRemaining = ChronoUnit.DAYS.between(today, goal.getEndDate());
            }
            
            // Selecionar cor para o gráfico
            String color = CHART_COLORS[colorIndex % CHART_COLORS.length];
            colorIndex++;
            
            // Criar dados da meta
            GoalsProgressChartDto.GoalData goalData = GoalsProgressChartDto.GoalData.builder()
                    .goalId(goal.getId())
                    .goalName(goal.getName())
                    .description(goal.getDescription())
                    .targetAmount(goal.getTargetAmount())
                    .currentAmount(goal.getCurrentAmount())
                    .progressPercentage(progressPercentage)
                    .achieved(goal.isAchieved())
                    .startDate(goal.getStartDate())
                    .endDate(goal.getEndDate())
                    .daysRemaining(daysRemaining)
                    .color(color)
                    .build();
            
            // Adicionar categoria se existir
            if (goal.getCategory() != null) {
                goalData.setCategoryId(goal.getCategory().getId());
                goalData.setCategoryName(goal.getCategory().getName());
            }
            
            goalsData.add(goalData);
        }
        
        // Ordenar por porcentagem de progresso descendente
        goalsData.sort((g1, g2) -> g2.getProgressPercentage().compareTo(g1.getProgressPercentage()));
        
        return GoalsProgressChartDto.builder()
                .goals(goalsData)
                .build();
    }
}
