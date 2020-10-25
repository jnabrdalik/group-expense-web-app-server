package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.ExpenseDetails;
import com.example.groupexpensewebapp.dto.ExpenseInput;
import com.example.groupexpensewebapp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/{id}")
    public ExpenseDetails getExpenseDetails(@PathVariable long id, Principal principal) {
        return expenseService.getExpenseDetails(id, principal.getName());
    }

    @PostMapping
    public ExpenseDetails addExpense(@RequestBody ExpenseInput input, Principal principal) {
        return expenseService.addExpense(input, principal.getName());
    }

    @PutMapping("/{id}")
    public ExpenseDetails editExpense(@PathVariable long id, @RequestBody ExpenseInput input, Principal principal) {
        return expenseService.editExpense(id, input, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable long id, Principal principal) {
        expenseService.deleteExpense(id, principal.getName());
    }
}
