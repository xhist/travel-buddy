package com.travelbuddy.controller;

import com.travelbuddy.dto.ExpenseRequest;
import com.travelbuddy.dto.ExpenseResponse;
import com.travelbuddy.model.Expense;
import com.travelbuddy.model.Trip;
import com.travelbuddy.service.interfaces.IExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/api/expenses")
@Slf4j
public class ExpenseController {

    @Autowired
    private IExpenseService expenseService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<Set<ExpenseResponse>> getTripExpenses(@PathVariable Long tripId) {
        final var tripExpenses = expenseService.getTripExpenses(tripId);
        return ResponseEntity.ok(tripExpenses);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<Set<ExpenseResponse>> logExpense(@Valid @RequestBody ExpenseRequest request) {
        final var updatedExpenses = expenseService.logExpense(request);
        return ResponseEntity.ok(updatedExpenses);
    }
}
