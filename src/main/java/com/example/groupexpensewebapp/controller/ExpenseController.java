package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.ExpenseDetails;
import com.example.groupexpensewebapp.dto.ExpenseInput;
import com.example.groupexpensewebapp.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{id}")
    public ExpenseDetails getExpenseDetails(@PathVariable long id) {
        return expenseService.getExpenseDetails(id);
    }

    @PostMapping
    public ExpenseDetails addExpense(@RequestBody ExpenseInput input) {
        return expenseService.addExpense(input);
    }

    @PutMapping("/{id}")
    public ExpenseDetails editExpense(@PathVariable long id, @RequestBody ExpenseInput input) {
        return expenseService.editExpense(id, input);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable long id) {
        expenseService.deleteExpense(id);
    }
}
