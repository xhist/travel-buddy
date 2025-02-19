package com.travelbuddy.service.interfaces;

import com.travelbuddy.dto.ExpenseRequest;
import com.travelbuddy.dto.ExpenseResponse;

import java.util.Set;

public interface IExpenseService {
    Set<ExpenseResponse> logExpense(final ExpenseRequest request);
    Set<ExpenseResponse> getTripExpenses(final Long tripId);
    void deleteExpense(final Long expenseId);
}
