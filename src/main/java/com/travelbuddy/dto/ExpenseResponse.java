package com.travelbuddy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private BigDecimal amount;
    private String reason;
    private Long tripId;
}
