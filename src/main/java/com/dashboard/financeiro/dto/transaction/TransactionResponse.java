package com.dashboard.financeiro.dto.transaction;

import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String type;
    private LocalDate date;
    private String description;
    private CategoryDto category;
    
    @Data
    public static class CategoryDto {
        private Long id;
        private String name;
        private String type;
    }
    
    public static TransactionResponse fromEntity(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType().getDescription());
        response.setDate(transaction.getDate());
        response.setDescription(transaction.getDescription());
        
        CategoryDto categoryDto = new CategoryDto();
        Category category = transaction.getCategory();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        categoryDto.setType(category.getType().getDescription());
        
        response.setCategory(categoryDto);
        
        return response;
    }
}
