package com.dashboard.financeiro.service;

import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.FinancialGoal;
import com.dashboard.financeiro.model.Transaction;
import com.dashboard.financeiro.model.User;
import com.dashboard.financeiro.repository.FinancialGoalRepository;
import com.dashboard.financeiro.repository.TransactionRepository;
import com.dashboard.financeiro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FinancialSummaryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinancialGoalRepository financialGoalRepository;

    @InjectMocks
    private FinancialSummaryService financialSummaryService;

    private User testUser;
    private Category expenseCategory;
    private Category incomeCategory;
    private FinancialGoal achievedGoal;
    private List<Object[]> expenseSummaries;
    private List<Object[]> incomeSummaries;

    @BeforeEach
    void setUp() {
        // Configurar dados de teste
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        expenseCategory = new Category();
        expenseCategory.setId(1L);
        expenseCategory.setName("Alimentação");
        expenseCategory.setUser(testUser);

        incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setName("Salário");
        incomeCategory.setUser(testUser);

        achievedGoal = new FinancialGoal();
        achievedGoal.setId(1L);
        achievedGoal.setName("Meta de economia");
        achievedGoal.setDescription("Economizar para viagem");
        achievedGoal.setTargetAmount(new BigDecimal("1000.00"));
        achievedGoal.setCurrentAmount(new BigDecimal("1000.00"));
        achievedGoal.setAchieved(true);
        achievedGoal.setCategory(expenseCategory);
        achievedGoal.setUser(testUser);

        // Configurar dados de resumo por categoria
        expenseSummaries = new ArrayList<>();
        expenseSummaries.add(new Object[]{1L, "Alimentação", new BigDecimal("500.00")});

        incomeSummaries = new ArrayList<>();
        incomeSummaries.add(new Object[]{2L, "Salário", new BigDecimal("1500.00")});
    }

    @Test
    @DisplayName("Deve obter resumo financeiro com sucesso")
    void shouldGetFinancialSummary() {
        // Configuração
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                eq(testUser), 
                eq(Transaction.TransactionType.INCOME), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(new BigDecimal("1500.00"));
        
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                eq(testUser), 
                eq(Transaction.TransactionType.EXPENSE), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(new BigDecimal("500.00"));
        
        when(financialGoalRepository.findByUserAndAchievedTrue(testUser))
                .thenReturn(Arrays.asList(achievedGoal));
        
        when(transactionRepository.sumByUserAndTypeAndDateBetweenGroupByCategory(
                eq(testUser), 
                eq(Transaction.TransactionType.EXPENSE), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(expenseSummaries);
        
        when(transactionRepository.sumByUserAndTypeAndDateBetweenGroupByCategory(
                eq(testUser), 
                eq(Transaction.TransactionType.INCOME), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(incomeSummaries);

        // Execução
        FinancialSummaryResponse summary = financialSummaryService.getFinancialSummary(
                "testuser", startDate, endDate);

        // Verificações
        assertNotNull(summary);
        assertEquals(new BigDecimal("1000.00"), summary.getCurrentBalance());
        assertEquals(new BigDecimal("1500.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("500.00"), summary.getTotalExpense());
        assertEquals("BRL", summary.getCurrency());
        
        // Verificar categorias de despesa
        assertNotNull(summary.getExpensesByCategory());
        assertEquals(1, summary.getExpensesByCategory().size());
        assertEquals(1L, summary.getExpensesByCategory().get(0).getCategoryId());
        assertEquals("Alimentação", summary.getExpensesByCategory().get(0).getCategoryName());
        assertEquals(new BigDecimal("500.00"), summary.getExpensesByCategory().get(0).getAmount());
        
        // Verificar categorias de receita
        assertNotNull(summary.getIncomesByCategory());
        assertEquals(1, summary.getIncomesByCategory().size());
        assertEquals(2L, summary.getIncomesByCategory().get(0).getCategoryId());
        assertEquals("Salário", summary.getIncomesByCategory().get(0).getCategoryName());
        assertEquals(new BigDecimal("1500.00"), summary.getIncomesByCategory().get(0).getAmount());
        
        // Verificar metas financeiras
        assertNotNull(summary.getAchievedGoals());
        assertEquals(1, summary.getAchievedGoals().size());
        assertEquals(1L, summary.getAchievedGoals().get(0).getId());
        assertEquals("Meta de economia", summary.getAchievedGoals().get(0).getName());
        assertEquals(new BigDecimal("1000.00"), summary.getAchievedGoals().get(0).getTargetAmount());
        assertTrue(summary.getAchievedGoals().get(0).isAchieved());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Configuração
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Execução e Verificação
        assertThrows(RuntimeException.class, () -> {
            financialSummaryService.getFinancialSummary("nonexistent", LocalDate.now(), LocalDate.now());
        });
    }

    @Test
    @DisplayName("Deve usar datas padrão quando não fornecidas")
    void shouldUseDefaultDatesWhenNotProvided() {
        // Configuração
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                any(User.class), 
                any(Transaction.TransactionType.class), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        
        when(financialGoalRepository.findByUserAndAchievedTrue(any(User.class)))
                .thenReturn(new ArrayList<>());
        
        when(transactionRepository.sumByUserAndTypeAndDateBetweenGroupByCategory(
                any(User.class), 
                any(Transaction.TransactionType.class), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        // Execução
        financialSummaryService.getFinancialSummary("testuser", null, null);

        // Verificação
        verify(transactionRepository, times(2)).sumByUserAndTypeAndDateBetween(
                eq(testUser), 
                any(Transaction.TransactionType.class), 
                any(LocalDate.class),  // Verificar se uma data foi usada
                any(LocalDate.class)); // Verificar se uma data foi usada
    }

    @Test
    @DisplayName("Deve lidar corretamente com valores nulos de transações")
    void shouldHandleNullTransactionValues() {
        // Configuração
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        when(transactionRepository.sumByUserAndTypeAndDateBetween(
                eq(testUser), 
                any(Transaction.TransactionType.class), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(null); // Retornar null para simular ausência de transações
        
        when(financialGoalRepository.findByUserAndAchievedTrue(any(User.class)))
                .thenReturn(new ArrayList<>());
        
        when(transactionRepository.sumByUserAndTypeAndDateBetweenGroupByCategory(
                any(User.class), 
                any(Transaction.TransactionType.class), 
                any(LocalDate.class), 
                any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        // Execução
        FinancialSummaryResponse summary = financialSummaryService.getFinancialSummary(
                "testuser", LocalDate.now(), LocalDate.now());

        // Verificação
        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.getTotalIncome());
        assertEquals(BigDecimal.ZERO, summary.getTotalExpense());
        assertEquals(BigDecimal.ZERO, summary.getCurrentBalance());
    }
}
