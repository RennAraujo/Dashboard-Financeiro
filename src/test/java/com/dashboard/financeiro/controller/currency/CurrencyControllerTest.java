package com.dashboard.financeiro.controller.currency;

import com.dashboard.financeiro.dto.currency.CurrencyConversionRequest;
import com.dashboard.financeiro.dto.currency.CurrencyConversionResponse;
import com.dashboard.financeiro.dto.currency.FinancialSummaryConversionRequest;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.service.currency.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrencyService currencyService;

    @Test
    @DisplayName("Deve converter moeda com sucesso")
    @WithMockUser(username = "testuser")
    public void shouldConvertCurrency() throws Exception {
        // Configuração
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setFromCurrency("BRL");
        request.setToCurrency("USD");

        CurrencyConversionResponse response = CurrencyConversionResponse.builder()
                .originalAmount(new BigDecimal("100.00"))
                .originalCurrency("BRL")
                .convertedAmount(new BigDecimal("20.00"))
                .targetCurrency("USD")
                .exchangeRate(new BigDecimal("0.20"))
                .conversionDate(LocalDateTime.now())
                .build();

        when(currencyService.convertCurrency(any(CurrencyConversionRequest.class))).thenReturn(response);

        // Execução e Verificação
        mockMvc.perform(post("/api/currency/convert")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalAmount").value(100.0))
                .andExpect(jsonPath("$.originalCurrency").value("BRL"))
                .andExpect(jsonPath("$.convertedAmount").value(20.0))
                .andExpect(jsonPath("$.targetCurrency").value("USD"))
                .andExpect(jsonPath("$.exchangeRate").value(0.20));
    }

    @Test
    @DisplayName("Deve retornar erro 401 quando não autenticado")
    public void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setFromCurrency("BRL");
        request.setToCurrency("USD");

        mockMvc.perform(post("/api/currency/convert")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve converter resumo financeiro com sucesso (POST)")
    @WithMockUser(username = "testuser")
    public void shouldConvertFinancialSummaryPost() throws Exception {
        // Configuração
        FinancialSummaryConversionRequest request = new FinancialSummaryConversionRequest();
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());
        request.setTargetCurrency("USD");

        FinancialSummaryResponse response = FinancialSummaryResponse.builder()
                .currentBalance(new BigDecimal("200.00"))
                .totalIncome(new BigDecimal("300.00"))
                .totalExpense(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        when(currencyService.convertFinancialSummary(
                anyString(), any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(response);

        // Execução e Verificação
        mockMvc.perform(post("/api/currency/convert-summary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(200.0))
                .andExpect(jsonPath("$.totalIncome").value(300.0))
                .andExpect(jsonPath("$.totalExpense").value(100.0))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("Deve converter resumo financeiro com sucesso (GET)")
    @WithMockUser(username = "testuser")
    public void shouldConvertFinancialSummaryGet() throws Exception {
        // Configuração
        FinancialSummaryResponse response = FinancialSummaryResponse.builder()
                .currentBalance(new BigDecimal("200.00"))
                .totalIncome(new BigDecimal("300.00"))
                .totalExpense(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        when(currencyService.convertFinancialSummary(
                anyString(), any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(response);

        // Execução e Verificação
        mockMvc.perform(get("/api/currency/convert-summary")
                        .with(csrf())
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString())
                        .param("targetCurrency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(200.0))
                .andExpect(jsonPath("$.totalIncome").value(300.0))
                .andExpect(jsonPath("$.totalExpense").value(100.0))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("Deve listar moedas disponíveis")
    @WithMockUser(username = "testuser")
    public void shouldListAvailableCurrencies() throws Exception {
        // Configuração
        Map<String, String> currencies = new HashMap<>();
        currencies.put("USD", "USD");
        currencies.put("BRL", "BRL");
        currencies.put("EUR", "EUR");

        when(currencyService.getAvailableCurrencies()).thenReturn(currencies);

        // Execução e Verificação
        mockMvc.perform(get("/api/currency/available")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD").value("USD"))
                .andExpect(jsonPath("$.BRL").value("BRL"))
                .andExpect(jsonPath("$.EUR").value("EUR"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para requisição inválida")
    @WithMockUser(username = "testuser")
    public void shouldReturnBadRequestForInvalidRequest() throws Exception {
        // Configuração - Requisição com valores inválidos
        CurrencyConversionRequest request = new CurrencyConversionRequest();
        request.setAmount(null); // Valor inválido
        request.setFromCurrency("BRL");
        request.setToCurrency("USD");

        // Execução e Verificação
        mockMvc.perform(post("/api/currency/convert")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
