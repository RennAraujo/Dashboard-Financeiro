package com.dashboard.financeiro.service.currency;

import com.dashboard.financeiro.config.cache.CacheConfig;
import com.dashboard.financeiro.dto.currency.CurrencyConversionRequest;
import com.dashboard.financeiro.dto.currency.CurrencyConversionResponse;
import com.dashboard.financeiro.dto.currency.ExchangeRateResponse;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.service.FinancialSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CurrencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private static final String DEFAULT_CURRENCY = "BRL";
    
    @Value("${openexchangerates.api.url:https://openexchangerates.org/api}")
    private String apiUrl;
    
    @Value("${openexchangerates.app.id:YOUR_APP_ID}")
    private String appId;
    
    private final WebClient webClient;
    
    @Autowired
    private FinancialSummaryService financialSummaryService;
    
    public CurrencyService() {
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * Busca as taxas de câmbio atuais da API Open Exchange Rates
     * Utiliza cache para evitar múltiplas chamadas à API
     */
    @Cacheable(CacheConfig.EXCHANGE_RATES_CACHE)
    public ExchangeRateResponse getExchangeRates() {
        logger.info("Buscando taxas de câmbio da API Open Exchange Rates");
        
        return webClient.get()
                .uri(apiUrl + "/latest.json?app_id={appId}", appId)
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .block();
    }
    
    /**
     * Converte um valor de uma moeda para outra
     */
    public CurrencyConversionResponse convertCurrency(CurrencyConversionRequest request) {
        if (request.getFromCurrency().equals(request.getToCurrency())) {
            // Se as moedas forem iguais, retornar o valor original
            return CurrencyConversionResponse.builder()
                    .originalAmount(request.getAmount())
                    .originalCurrency(request.getFromCurrency())
                    .convertedAmount(request.getAmount())
                    .targetCurrency(request.getToCurrency())
                    .exchangeRate(BigDecimal.ONE)
                    .conversionDate(LocalDateTime.now())
                    .build();
        }
        
        // Buscar as taxas de câmbio
        ExchangeRateResponse exchangeRates = getExchangeRates();
        
        // Extrair as taxas para as moedas de origem e destino
        Double fromRate = request.getFromCurrency().equals("USD") ? 1.0 : exchangeRates.getRates().get(request.getFromCurrency());
        Double toRate = request.getToCurrency().equals("USD") ? 1.0 : exchangeRates.getRates().get(request.getToCurrency());
        
        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Moeda não suportada");
        }
        
        // Calcular a taxa de câmbio entre as moedas
        BigDecimal exchangeRate = BigDecimal.valueOf(toRate / fromRate);
        
        // Converter o valor
        BigDecimal convertedAmount = request.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        
        // Criar a resposta
        return CurrencyConversionResponse.builder()
                .originalAmount(request.getAmount())
                .originalCurrency(request.getFromCurrency())
                .convertedAmount(convertedAmount)
                .targetCurrency(request.getToCurrency())
                .exchangeRate(exchangeRate)
                .conversionDate(LocalDateTime.now())
                .build();
    }
    
    /**
     * Converte um resumo financeiro para outra moeda
     */
    public FinancialSummaryResponse convertFinancialSummary(String username, LocalDate startDate, LocalDate endDate, String targetCurrency) {
        // Obter o resumo financeiro original
        FinancialSummaryResponse originalSummary = financialSummaryService.getFinancialSummary(username, startDate, endDate);
        
        // Se a moeda alvo for a mesma da moeda padrão (BRL), retornar o resumo original
        if (DEFAULT_CURRENCY.equals(targetCurrency)) {
            return originalSummary;
        }
        
        // Buscar as taxas de câmbio
        ExchangeRateResponse exchangeRates = getExchangeRates();
        
        // Calcular a taxa de conversão de BRL para a moeda alvo
        Double brlRate = exchangeRates.getRates().get(DEFAULT_CURRENCY);
        Double targetRate = targetCurrency.equals("USD") ? 1.0 : exchangeRates.getRates().get(targetCurrency);
        
        if (brlRate == null || targetRate == null) {
            throw new IllegalArgumentException("Moeda não suportada");
        }
        
        BigDecimal exchangeRate = BigDecimal.valueOf(targetRate / brlRate);
        
        // Converter os valores no resumo financeiro
        BigDecimal convertedCurrentBalance = originalSummary.getCurrentBalance().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal convertedTotalIncome = originalSummary.getTotalIncome().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal convertedTotalExpense = originalSummary.getTotalExpense().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        
        // Converter valores por categoria de despesa
        List<FinancialSummaryResponse.CategorySummaryDto> convertedExpensesByCategory = new ArrayList<>();
        if (originalSummary.getExpensesByCategory() != null) {
            convertedExpensesByCategory = originalSummary.getExpensesByCategory().stream()
                    .map(category -> {
                        BigDecimal convertedAmount = category.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
                        return new FinancialSummaryResponse.CategorySummaryDto(
                                category.getCategoryId(),
                                category.getCategoryName(),
                                convertedAmount,
                                category.getCategoryType()
                        );
                    })
                    .collect(Collectors.toList());
        }
        
        // Converter valores por categoria de receita
        List<FinancialSummaryResponse.CategorySummaryDto> convertedIncomesByCategory = new ArrayList<>();
        if (originalSummary.getIncomesByCategory() != null) {
            convertedIncomesByCategory = originalSummary.getIncomesByCategory().stream()
                    .map(category -> {
                        BigDecimal convertedAmount = category.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
                        return new FinancialSummaryResponse.CategorySummaryDto(
                                category.getCategoryId(),
                                category.getCategoryName(),
                                convertedAmount,
                                category.getCategoryType()
                        );
                    })
                    .collect(Collectors.toList());
        }
        
        // Converter valores das metas financeiras
        List<FinancialSummaryResponse.FinancialGoalDto> convertedGoals = new ArrayList<>();
        if (originalSummary.getAchievedGoals() != null) {
            convertedGoals = originalSummary.getAchievedGoals().stream()
                    .map(goal -> {
                        BigDecimal convertedTargetAmount = goal.getTargetAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal convertedCurrentAmount = goal.getCurrentAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
                        return new FinancialSummaryResponse.FinancialGoalDto(
                                goal.getId(),
                                goal.getName(),
                                goal.getDescription(),
                                convertedTargetAmount,
                                convertedCurrentAmount,
                                goal.isAchieved(),
                                goal.getCategory()
                        );
                    })
                    .collect(Collectors.toList());
        }
        
        // Criar um novo resumo financeiro com os valores convertidos
        return FinancialSummaryResponse.builder()
                .currentBalance(convertedCurrentBalance)
                .totalIncome(convertedTotalIncome)
                .totalExpense(convertedTotalExpense)
                .expensesByCategory(convertedExpensesByCategory)
                .incomesByCategory(convertedIncomesByCategory)
                .achievedGoals(convertedGoals)
                .currency(targetCurrency)
                .build();
    }
    
    /**
     * Retorna a lista de moedas disponíveis para conversão
     */
    public Map<String, String> getAvailableCurrencies() {
        ExchangeRateResponse exchangeRates = getExchangeRates();
        return exchangeRates.getRates().keySet().stream()
                .collect(Collectors.toMap(
                        currency -> currency,    // Código da moeda
                        currency -> currency     // Nome da moeda (simplificado, idealmente usaríamos uma API para nomes completos)
                ));
    }
}
