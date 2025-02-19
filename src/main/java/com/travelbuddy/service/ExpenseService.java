package com.travelbuddy.service;

import com.travelbuddy.dto.ExpenseRequest;
import com.travelbuddy.dto.ExpenseResponse;
import com.travelbuddy.exception.ResourceNotFoundException;
import com.travelbuddy.model.Expense;
import com.travelbuddy.repository.ExpenseRepository;
import com.travelbuddy.repository.TripRepository;
import com.travelbuddy.service.interfaces.IExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExpenseService implements IExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private TripRepository tripRepository;

    @Override
    public Set<ExpenseResponse> getTripExpenses(final Long tripId) {
        final var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip %d not found".formatted(tripId)));
        return expenseRepository.findByTripId(tripId).stream()
                .map(tripExpense -> ExpenseResponse.builder()
                        .id(tripExpense.getId())
                        .reason(tripExpense.getReason())
                        .amount(tripExpense.getAmount())
                        .tripId(trip.getId())
                        .build()).collect(Collectors.toSet());
    }

    @Override
    public void deleteExpense(Long expenseId) {
        final var expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense %d not found"
                        .formatted(expenseId)));
        expenseRepository.delete(expense);
    }

    @Override
    public Set<ExpenseResponse> logExpense(final ExpenseRequest request) {
        final var trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip %d not found".formatted(request.getTripId())));
        final var expense = Expense.builder()
                .amount(request.getAmount())
                .reason(request.getReason())
                .trip(trip)
                .build();
        expenseRepository.save(expense);
        log.info("Expense {} with total amount {} logged for trip {}.", request.getReason(), request.getAmount(), request.getTripId());
        return expenseRepository.findByTripId(trip.getId()).stream()
                .map(tripExpense -> ExpenseResponse.builder()
                        .id(tripExpense.getId())
                        .reason(tripExpense.getReason())
                        .amount(tripExpense.getAmount())
                        .tripId(trip.getId())
                        .build()).collect(Collectors.toSet());
    }
}
