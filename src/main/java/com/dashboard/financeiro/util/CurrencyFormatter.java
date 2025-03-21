package com.dashboard.financeiro.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Classe utilitária para formatação de valores monetários
 */
public class CurrencyFormatter {

    private static final Map<String, Locale> CURRENCY_LOCALES = new HashMap<>();
    
    static {
        // Mapeamento de códigos de moeda para locales
        CURRENCY_LOCALES.put("USD", Locale.US);
        CURRENCY_LOCALES.put("EUR", Locale.FRANCE);
        CURRENCY_LOCALES.put("GBP", Locale.UK);
        CURRENCY_LOCALES.put("JPY", Locale.JAPAN);
        CURRENCY_LOCALES.put("BRL", new Locale("pt", "BR"));
        // Adicione mais conforme necessário
    }
    
    /**
     * Formata um valor monetário de acordo com a moeda especificada
     * 
     * @param amount o valor a ser formatado
     * @param currencyCode o código ISO da moeda (ex: USD, EUR, BRL)
     * @return string formatada com símbolo da moeda e separadores adequados
     */
    public static String format(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return "";
        }
        
        Locale locale = CURRENCY_LOCALES.getOrDefault(currencyCode, Locale.US);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        
        try {
            formatter.setCurrency(Currency.getInstance(currencyCode));
        } catch (IllegalArgumentException e) {
            // Se o código da moeda for inválido, usa o padrão do locale
        }
        
        return formatter.format(amount);
    }
    
    /**
     * Formata um valor monetário sem o símbolo da moeda
     * 
     * @param amount o valor a ser formatado
     * @param currencyCode o código ISO da moeda (ex: USD, EUR, BRL)
     * @return string formatada com separadores adequados, sem símbolo
     */
    public static String formatWithoutSymbol(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return "";
        }
        
        Locale locale = CURRENCY_LOCALES.getOrDefault(currencyCode, Locale.US);
        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        
        return formatter.format(amount);
    }
    
    /**
     * Retorna o símbolo da moeda para o código informado
     * 
     * @param currencyCode o código ISO da moeda (ex: USD, EUR, BRL)
     * @return o símbolo da moeda (ex: $, €, R$)
     */
    public static String getSymbol(String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            Locale locale = CURRENCY_LOCALES.getOrDefault(currencyCode, Locale.US);
            return currency.getSymbol(locale);
        } catch (IllegalArgumentException e) {
            return currencyCode; // Retorna o próprio código se não encontrar o símbolo
        }
    }
}
