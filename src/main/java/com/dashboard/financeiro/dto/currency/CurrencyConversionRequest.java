package com.dashboard.financeiro.dto.currency;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversionRequest {
    
    private BigDecimal amount;
    private String fromCurrency; // Código da moeda de origem (ex: BRL)
    private String toCurrency;   // Código da moeda de destino (ex: USD)
}
