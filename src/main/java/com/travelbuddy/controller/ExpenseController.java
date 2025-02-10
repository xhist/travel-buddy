package com.travelbuddy.controller;

import com.travelbuddy.dto.ExpenseRequest;
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

@RestController
@RequestMapping("/api/expenses")
@Slf4j
public class ExpenseController {

    @Autowired
    private IExpenseService expenseService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<?> logExpense(@Valid @RequestBody ExpenseRequest request) {
        Trip trip = new Trip();
        trip.setId(request.getTripId());
        Expense expense = Expense.builder()
                .payer(request.getPayer())
                .amount(request.getAmount())
                .purpose(request.getPurpose())
                .trip(trip)
                .dateLogged(LocalDateTime.now())
                .build();
        Expense loggedExpense = expenseService.logExpense(expense);
        return ResponseEntity.ok(loggedExpense);
    }
}
