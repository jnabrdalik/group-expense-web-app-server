package com.example.groupexpensewebapp.debt;

import com.example.groupexpensewebapp.model.Expense;

import java.util.List;

public interface DebtCalculator {
    List<Debt> calculateDebts(Iterable<Expense> expenses);
}
