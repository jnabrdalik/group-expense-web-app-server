package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/{id}/history")
    public List<ExpenseChange> getExpenseHistory(@PathVariable long id, Principal principal) {
        return expenseService.getExpenseHistory(id, principal != null ? principal.getName() : null);
    }

    @PostMapping("/{groupId}")
    public ExpenseDetails addExpense(@RequestBody ExpenseInput input, @PathVariable long groupId, Principal principal) {
        return expenseService.addExpense(input, groupId, principal != null ? principal.getName() : null);
    }

    @PutMapping("/{id}")
    public ExpenseDetails editExpense(@PathVariable long id, @RequestBody ExpenseInput input, Principal principal) {
        return expenseService.editExpense(id, input, principal != null ? principal.getName() : null);
    }

    @PostMapping("/{id}/revert")
    public ExpenseDetails revertLastChange(@PathVariable long id, Principal principal) {
        return expenseService.revertLastChange(id, principal != null ? principal.getName() : null);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable long id, Principal principal) {
        expenseService.deleteExpense(id, principal != null ? principal.getName() : null);
    }
}
