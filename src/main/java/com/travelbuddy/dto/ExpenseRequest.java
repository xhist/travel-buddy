package com.travelbuddy.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ExpenseRequest {
    @NotBlank
    private String payer;
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String purpose;
    @NotNull
    private Long tripId;
}
