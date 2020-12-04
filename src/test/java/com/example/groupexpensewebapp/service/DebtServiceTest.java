package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.Debt;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Involvement;
import com.example.groupexpensewebapp.model.Member;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DebtServiceTest {

    private final DebtService debtService = new DebtService(new ModelMapper());

    @Test
    public void shouldReturnOneDebtWhenTwoMembers() {
        Member member1 = new Member();
        member1.setId(1);
        Member member2 = new Member();
        member2.setId(2);

        Expense expense1 = new Expense();
        expense1.setAmount(1000);
        expense1.setPayer(member1);
        Involvement involvement1 = new Involvement();
        involvement1.setPayee(member2);
        involvement1.setWeight(1);
        expense1.setInvolvements(Collections.singleton(involvement1));

        Expense expense2 = new Expense();
        expense2.setAmount(2000);
        expense2.setPayer(member2);
        Involvement involvement2 = new Involvement();
        involvement2.setPayee(member1);
        involvement2.setWeight(1);
        expense2.setInvolvements(Collections.singleton(involvement2));

        List<Debt> debts = debtService.getDebts(List.of(expense1, expense2));

        assertEquals(debts.size(), 1);
        Debt debt = debts.get(0);

        assertEquals(debt.getAmount(), 1000);
        assertEquals(debt.getDebtor().getId(), member1.getId());
        assertEquals(debt.getCreditor().getId(), member2.getId());

    }

    @Test
    public void shouldReturnNoDebtsWhenExpensesAreOpposite() {
        Member member1 = new Member();
        member1.setId(1);
        Member member2 = new Member();
        member2.setId(2);

        Expense expense1 = new Expense();
        expense1.setAmount(1000);
        expense1.setPayer(member1);

        Involvement involvement1 = new Involvement();
        involvement1.setPayee(member2);
        involvement1.setWeight(1);

        expense1.setInvolvements(Collections.singleton(involvement1));

        Expense expense2 = new Expense();
        expense2.setAmount(1000);
        expense2.setPayer(member2);

        Involvement involvement2 = new Involvement();
        involvement2.setPayee(member1);
        involvement2.setWeight(1);

        expense2.setInvolvements(Collections.singleton(involvement2));
        List<Debt> debts = debtService.getDebts(List.of(expense1, expense2));

        assertEquals(debts.size(), 0);
    }

    @Test
    public void shouldReturnNoDebtsWhenNoExpenses() {
        List<Expense> expenses = new ArrayList<>();
        List<Debt> debts = debtService.getDebts(expenses);

        assertEquals(debts.size(), 0);
    }

    @Test
    public void shouldReturnDebtsWithDifferentAmountsWhenWeightsIncluded() {
        Member member1 = new Member();
        member1.setId(1);
        Member member2 = new Member();
        member2.setId(2);
        Member member3 = new Member();
        member3.setId(3);

        Expense expense = new Expense();
        expense.setAmount(1000);
        expense.setPayer(member1);

        Involvement involvement1 = new Involvement();
        involvement1.setPayee(member2);
        involvement1.setWeight(1);

        Involvement involvement2 = new Involvement();
        involvement2.setPayee(member3);
        involvement2.setWeight(2);

        expense.setInvolvements(Set.of(involvement1, involvement2));
        List<Debt> debts = debtService.getDebts(List.of(expense));

        assertEquals(debts.size(), 2);
        assertTrue(debts.stream().anyMatch(d -> d.getAmount() == 333 && d.getDebtor().getId() == member2.getId()));
        assertTrue(debts.stream().anyMatch(d -> d.getAmount() == 667 && d.getDebtor().getId() == member3.getId()));
    }

}