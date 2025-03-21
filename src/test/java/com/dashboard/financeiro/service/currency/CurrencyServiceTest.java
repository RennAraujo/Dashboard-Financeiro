package com.dashboard.financeiro.service.currency;

import com.dashboard.financeiro.dto.currency.CurrencyConversionRequest;
import com.dashboard.financeiro.dto.currency.CurrencyConversionResponse;
import com.dashboard.financeiro.dto.currency.ExchangeRateResponse;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse.CategorySummaryDto;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse.FinancialGoalDto;
import com.dashboard.financeiro.service.FinancialSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    @Mock
    private FinancialSummaryService financialSummaryService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CurrencyService currencyService;

    private ExchangeRateResponse mockExchangeRateResponse;

    @BeforeEach
    void setUp() {
        // Mock de WebClient
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Configuração de dados de teste
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("BRL", 5.0);
        rates.put("EUR", 0.85);
        rates.put("GBP", 0.75);

        mockExchangeRateResponse = new ExchangeRateResponse();
        mockExchangeRateResponse.setBase("USD");
        mockExchangeRateResponse.setRates(rates);

        when(responseSpec.bodyToMono(ExchangeRateResponse.class)).thenReturn(Mono.just(mockExchangeRateResponse));

        // Substituir o WebClient real pelo mock
        ReflectionTestUtils.setField(currencyService, "webClient", webClient);
        ReflectionTestUtils.setField(currencyService, "apiUrl", "https://testapi.com");
        ReflectionTestUtils.setField(currencyService, "appId", "test-app-id");
    }

    @Test
    @DisplayName("Deve obter taxas de câmbio com sucesso")
    void shouldGetExchangeRates() {
        // Execução
        ExchangeRateResponse result = currencyService.getExchangeRates();

        // Verificações
        assertNotNull(result);
        assertEquals("USD", result.getBase());
        assertEquals(4, result.getRates().size());
        assertEquals(1.0, result.getRates().get("USD"));
        assertEquals(5.0, result.getRates().get("BRL"));
    }

    @Test
    @DisplayName("Deve converter valor entre moedas com sucesso")
    void shouldConvertCurrency() {
        // Configuração
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setFromCurrency("BRL");
        request.setToCurrency("USD");

        // Execução
        CurrencyConversionResponse response = currencyService.convertCurrency(request);

        // Verificações
        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getOriginalAmount());
        assertEquals("BRL", response.getOriginalCurrency());
        assertEquals("USD", response.getTargetCurrency());
        
        // Taxa de BRL para USD: USD 1.0 / BRL 5.0 = 0.2
        // 100 BRL * 0.2 = 20 USD
        assertEquals(new BigDecimal("20.00").setScale(2, RoundingMode.HALF_UP), response.getConvertedAmount());
    }

    @Test
    @DisplayName("Deve retornar mesmo valor quando as moedas são iguais")
    void shouldReturnSameAmountWhenCurrenciesAreEqual() {
        // Configuração
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setFromCurrency("USD");
        request.setToCurrency("USD");

        // Execução
        CurrencyConversionResponse response = currencyService.convertCurrency(request);

        // Verificações
        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getOriginalAmount());
        assertEquals(new BigDecimal("100.00"), response.getConvertedAmount());
        assertEquals(BigDecimal.ONE, response.getExchangeRate());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a moeda não é suportada")
    void shouldThrowExceptionWhenCurrencyNotSupported() {
        // Configuração
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setFromCurrency("BRL");
        request.setToCurrency("XYZ"); // Moeda não existente

        // Verificação e execução
        assertThrows(IllegalArgumentException.class, () -> currencyService.convertCurrency(request));
    }

    @Test
    @DisplayName("Deve converter resumo financeiro para outra moeda")
    void shouldConvertFinancialSummary() {
        // Configuração
        FinancialSummaryResponse originalSummary = createMockFinancialSummary();
        when(financialSummaryService.getFinancialSummary(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(originalSummary);

        // Execução
        FinancialSummaryResponse convertedSummary = currencyService.convertFinancialSummary(
                "testuser", LocalDate.now(), LocalDate.now(), "USD");

        // Verificações
        assertNotNull(convertedSummary);
        assertEquals("USD", convertedSummary.getCurrency());
        
        // Verificar conversão de BRL para USD (taxa: 0.2)
        assertEquals(new BigDecimal("200.00").setScale(2, RoundingMode.HALF_UP), convertedSummary.getCurrentBalance());
        assertEquals(new BigDecimal("300.00").setScale(2, RoundingMode.HALF_UP), convertedSummary.getTotalIncome());
        assertEquals(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP), convertedSummary.getTotalExpense());
        
        // Verificar categorias convertidas
        assertNotNull(convertedSummary.getExpensesByCategory());
        assertEquals(1, convertedSummary.getExpensesByCategory().size());
        assertEquals(new BigDecimal("100.00").setScale(2), convertedSummary.getExpensesByCategory().get(0).getAmount());
        
        // Verificar metas financeiras convertidas
        assertNotNull(convertedSummary.getAchievedGoals());
        assertEquals(1, convertedSummary.getAchievedGoals().size());
        assertEquals(new BigDecimal("200.00").setScale(2), convertedSummary.getAchievedGoals().get(0).getTargetAmount());
        assertEquals(new BigDecimal("200.00").setScale(2), convertedSummary.getAchievedGoals().get(0).getCurrentAmount());
    }

    @Test
    @DisplayName("Deve retornar resumo original quando a moeda alvo é a padrão")
    void shouldReturnOriginalSummaryWhenTargetCurrencyIsDefault() {
        // Configuração
        FinancialSummaryResponse originalSummary = createMockFinancialSummary();
        when(financialSummaryService.getFinancialSummary(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(originalSummary);

        // Execução
        FinancialSummaryResponse result = currencyService.convertFinancialSummary(
                "testuser", LocalDate.now(), LocalDate.now(), "BRL");

        // Verificações
        assertNotNull(result);
        assertSame(originalSummary, result);
    }

    @Test
    @DisplayName("Deve obter lista de moedas disponíveis")
    void shouldGetAvailableCurrencies() {
        // Execução
        Map<String, String> currencies = currencyService.getAvailableCurrencies();

        // Verificações
        assertNotNull(currencies);
        assertEquals(4, currencies.size());
        assertTrue(currencies.containsKey("USD"));
        assertTrue(currencies.containsKey("BRL"));
        assertTrue(currencies.containsKey("EUR"));
        assertTrue(currencies.containsKey("GBP"));
    }

    /**
     * Cria um mock de resumo financeiro para testes
     */
    private FinancialSummaryResponse createMockFinancialSummary() {
        CategorySummaryDto expenseCategory = CategorySummaryDto.builder()
                .categoryId(1L)
                .categoryName("Alimentação")
                .amount(new BigDecimal("500.00"))
                .categoryType("EXPENSE")
                .build();

        CategorySummaryDto incomeCategory = CategorySummaryDto.builder()
                .categoryId(2L)
                .categoryName("Salário")
                .amount(new BigDecimal("1500.00"))
                .categoryType("INCOME")
                .build();

        FinancialGoalDto goal = FinancialGoalDto.builder()
                .id(1L)
                .name("Meta de economia")
                .description("Economizar para viagem")
                .targetAmount(new BigDecimal("1000.00"))
                .currentAmount(new BigDecimal("1000.00"))
                .achieved(true)
                .category("Viagem")
                .build();

        return FinancialSummaryResponse.builder()
                .currentBalance(new BigDecimal("1000.00"))
                .totalIncome(new BigDecimal("1500.00"))
                .totalExpense(new BigDecimal("500.00"))
                .expensesByCategory(Arrays.asList(expenseCategory))
                .incomesByCategory(Arrays.asList(incomeCategory))
                .achievedGoals(Arrays.asList(goal))
                .currency("BRL")
                .build();
    }
}
