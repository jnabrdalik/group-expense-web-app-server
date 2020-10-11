package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.Expense;
import org.springframework.data.repository.CrudRepository;

public interface ExpenseRepository extends CrudRepository<Expense, Long> {

    Iterable<Expense> findAllByGroup_Id(long groupId);
}
