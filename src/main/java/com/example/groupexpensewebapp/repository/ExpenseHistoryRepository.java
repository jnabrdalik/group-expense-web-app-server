package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.ExpenseHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ExpenseHistoryRepository extends CrudRepository<ExpenseHistory, Long> {

    Optional<ExpenseHistory> findFirstByOriginalExpense_IdOrderByTimeCreatedDesc(long expenseId);

    ExpenseHistory findFirstByOriginalExpense_IdOrderByTimeCreatedAsc(long expenseId);
}
