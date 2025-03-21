package com.dashboard.financeiro.controller.currency;

import com.dashboard.financeiro.dto.currency.CurrencyConversionRequest;
import com.dashboard.financeiro.dto.currency.CurrencyConversionResponse;
import com.dashboard.financeiro.dto.currency.FinancialSummaryConversionRequest;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.service.currency.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    /**
     * Endpoint para converter um valor de uma moeda para outra
     */
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convertCurrency(
            @RequestBody CurrencyConversionRequest request) {
        
        CurrencyConversionResponse response = currencyService.convertCurrency(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obter o resumo financeiro convertido para outra moeda
     */
    @PostMapping("/convert-summary")
    public ResponseEntity<FinancialSummaryResponse> convertFinancialSummary(
            Authentication authentication,
            @RequestBody FinancialSummaryConversionRequest request) {
        
        String username = authentication.getName();
        
        FinancialSummaryResponse convertedSummary = currencyService.convertFinancialSummary(
                username,
                request.getStartDate(),
                request.getEndDate(),
                request.getTargetCurrency()
        );
        
        return ResponseEntity.ok(convertedSummary);
    }

    /**
     * Endpoint alternativo para obter o resumo financeiro convertido usando parâmetros de consulta
     */
    @GetMapping("/convert-summary")
    public ResponseEntity<FinancialSummaryResponse> convertFinancialSummaryGet(
            Authentication authentication,
            @RequestParam(value = "startDate", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("targetCurrency") String targetCurrency) {
        
        String username = authentication.getName();
        
        // Se as datas não forem fornecidas, usar o mês atual
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        FinancialSummaryResponse convertedSummary = currencyService.convertFinancialSummary(
                username,
                startDate,
                endDate,
                targetCurrency
        );
        
        return ResponseEntity.ok(convertedSummary);
    }

    /**
     * Endpoint para obter a lista de moedas disponíveis para conversão
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, String>> getAvailableCurrencies() {
        Map<String, String> currencies = currencyService.getAvailableCurrencies();
        return ResponseEntity.ok(currencies);
    }
}
