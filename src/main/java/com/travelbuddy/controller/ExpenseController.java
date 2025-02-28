package com.travelbuddy.controller;

import com.travelbuddy.dto.ExpenseRequest;
import com.travelbuddy.dto.ExpenseResponse;
import com.travelbuddy.model.Expense;
import com.travelbuddy.model.Trip;
import com.travelbuddy.security.SecurityEvaluator;
import com.travelbuddy.service.interfaces.IExpenseService;
import com.travelbuddy.service.interfaces.ITripService;
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
    @Autowired
    private SecurityEvaluator securityEvaluator;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{tripId}")
    public ResponseEntity<Set<ExpenseResponse>> getTripExpenses(@PathVariable Long tripId) {
        final var tripExpenses = expenseService.getTripExpenses(tripId);
        return ResponseEntity.ok(tripExpenses);
    }

    @PreAuthorize("@securityEvaluator.isOrganizer(#request.tripId, authentication.principal.id)")
    @PostMapping
    public ResponseEntity<Set<ExpenseResponse>> logExpense(@Valid @RequestBody ExpenseRequest request) {
        final var updatedExpenses = expenseService.logExpense(request);
        return ResponseEntity.ok(updatedExpenses);
    }

    @PreAuthorize("@securityEvaluator.isOrganizerForExpense(#expenseId, authentication.principal.id)")
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable final Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.ok("Expense deleted successfully");
    }
}
