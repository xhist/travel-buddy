package com.travelbuddy.service;

import com.travelbuddy.model.Expense;
import com.travelbuddy.repository.ExpenseRepository;
import com.travelbuddy.service.interfaces.IExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ExpenseService implements IExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public Expense logExpense(Expense expense) {
        expense.setDateLogged(LocalDateTime.now());
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense logged: {} by {}", expense.getPurpose(), expense.getPayer());
        return savedExpense;
    }
}
