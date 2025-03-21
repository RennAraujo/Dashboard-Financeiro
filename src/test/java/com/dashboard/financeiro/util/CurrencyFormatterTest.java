package com.dashboard.financeiro.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyFormatterTest {

    @Test
    @DisplayName("Deve formatar valores monetários com o símbolo da moeda")
    public void shouldFormatAmountWithCurrencySymbol() {
        // Configuração
        BigDecimal amount = new BigDecimal("1234.56");
        
        // Execução e Verificação para diferentes moedas
        String formattedUSD = CurrencyFormatter.format(amount, "USD");
        assertTrue(formattedUSD.contains("$"), "Deve conter símbolo do dólar");
        assertTrue(formattedUSD.contains("1,234.56"), "Deve estar formatado corretamente");
        
        String formattedBRL = CurrencyFormatter.format(amount, "BRL");
        assertTrue(formattedBRL.contains("R$"), "Deve conter símbolo do real");
    }
    
    @Test
    @DisplayName("Deve formatar valores monetários sem o símbolo da moeda")
    public void shouldFormatAmountWithoutCurrencySymbol() {
        // Configuração
        BigDecimal amount = new BigDecimal("1234.56");
        
        // Execução
        String formatted = CurrencyFormatter.formatWithoutSymbol(amount, "USD");
        
        // Verificação
        assertFalse(formatted.contains("$"), "Não deve conter símbolo do dólar");
        assertTrue(formatted.contains("1,234.56"), "Deve estar formatado corretamente");
    }
    
    @Test
    @DisplayName("Deve retornar string vazia quando o valor é nulo")
    public void shouldReturnEmptyStringWhenAmountIsNull() {
        // Execução e Verificação
        assertEquals("", CurrencyFormatter.format(null, "USD"));
        assertEquals("", CurrencyFormatter.formatWithoutSymbol(null, "USD"));
    }
    
    @ParameterizedTest
    @CsvSource({
        "USD, $",
        "BRL, R$",
        "EUR, €",
        "GBP, £",
        "JPY, ¥"
    })
    @DisplayName("Deve retornar o símbolo correto para cada moeda")
    public void shouldReturnCorrectSymbolForCurrency(String currencyCode, String expectedSymbol) {
        // Execução
        String symbol = CurrencyFormatter.getSymbol(currencyCode);
        
        // Verificação
        assertTrue(symbol.contains(expectedSymbol), 
                "O símbolo " + symbol + " deve conter " + expectedSymbol + " para a moeda " + currencyCode);
    }
    
    @Test
    @DisplayName("Deve retornar o código da moeda quando o código é inválido")
    public void shouldReturnCurrencyCodeWhenCodeIsInvalid() {
        // Execução
        String symbol = CurrencyFormatter.getSymbol("XYZ");
        
        // Verificação
        assertEquals("XYZ", symbol, "Deve retornar o próprio código quando a moeda é inválida");
    }
    
    @Test
    @DisplayName("Deve formatar valores negativos corretamente")
    public void shouldFormatNegativeAmountsCorrectly() {
        // Configuração
        BigDecimal negativeAmount = new BigDecimal("-1234.56");
        
        // Execução
        String formatted = CurrencyFormatter.format(negativeAmount, "USD");
        
        // Verificação
        assertTrue(formatted.contains("-"), "Deve conter sinal negativo");
        assertTrue(formatted.contains("1,234.56"), "Deve estar formatado corretamente");
    }
}
