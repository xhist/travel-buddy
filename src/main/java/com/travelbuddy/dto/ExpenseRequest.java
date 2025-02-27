package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ExpenseRequest {
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String reason;
    @NotNull
    private Long tripId;
}
