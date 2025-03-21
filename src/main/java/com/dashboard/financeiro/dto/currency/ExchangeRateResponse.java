package com.dashboard.financeiro.dto.currency;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para resposta da API Open Exchange Rates
 * Exemplo de resposta:
 * {
 *   "disclaimer": "Usage subject to terms: https://openexchangerates.org/terms",
 *   "license": "https://openexchangerates.org/license",
 *   "timestamp": 1617196800,
 *   "base": "USD",
 *   "rates": {
 *     "AED": 3.673,
 *     "AFN": 78.25,
 *     "BRL": 5.6312,
 *     ...
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {
    
    private String disclaimer;
    private String license;
    private long timestamp;
    private String base;
    
    @JsonProperty("rates")
    private Map<String, Double> rates;
}
