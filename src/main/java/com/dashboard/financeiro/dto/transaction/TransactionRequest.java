package com.dashboard.financeiro.dto.transaction;

import com.dashboard.financeiro.model.Transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    private BigDecimal amount;

    @NotNull(message = "O tipo de transação é obrigatório")
    private TransactionType type;

    @NotNull(message = "A data é obrigatória")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String description;

    @NotNull(message = "A categoria é obrigatória")
    private Long categoryId;
}
