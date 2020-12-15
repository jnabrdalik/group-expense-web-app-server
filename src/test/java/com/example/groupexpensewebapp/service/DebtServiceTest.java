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
        Member memberA = new Member();
        memberA.setId(1);
        Member memberB = new Member();
        memberB.setId(2);
        Member memberC = new Member();
        memberC.setId(3);

        Expense firstExpense = new Expense();
        firstExpense.setAmount(1000);
        firstExpense.setPayer(memberA);
        Involvement firstExpenseInvolvement1 = new Involvement();
        firstExpenseInvolvement1.setPayee(memberB);
        firstExpenseInvolvement1.setWeight(1);
        firstExpense.setInvolvements(Collections.singleton(firstExpenseInvolvement1));

        Expense secondExpense = new Expense();
        secondExpense.setAmount(2000);
        secondExpense.setPayer(memberA);
        Involvement secondExpenseInvolvement1 = new Involvement();
        secondExpenseInvolvement1.setPayee(memberA);
        secondExpenseInvolvement1.setWeight(1);
        Involvement secondExpenseInvolvement2 = new Involvement();
        secondExpenseInvolvement2.setPayee(memberC);
        secondExpenseInvolvement2.setWeight(1);
        secondExpense.setInvolvements(Set.of(secondExpenseInvolvement1, secondExpenseInvolvement2));

        Expense thirdExpense = new Expense();
        thirdExpense.setAmount(2000);
        thirdExpense.setPayer(memberB);
        Involvement thirdExpenseInvolvement1 = new Involvement();
        thirdExpenseInvolvement1.setPayee(memberA);
        thirdExpenseInvolvement1.setWeight(1);
        thirdExpense.setInvolvements(Collections.singleton(thirdExpenseInvolvement1));

        List<Debt> debts = debtService.calculateDebts(List.of(firstExpense, secondExpense, thirdExpense));

        assertEquals(debts.size(), 2);
        Debt debt = debts.get(0);

        assertTrue(debts.stream()
                .anyMatch(d -> d.getAmount() == 1000 &&
                        d.getDebtor().getId() == memberA.getId() &&
                        d.getCreditor().getId() == memberB.getId()));

        assertTrue(debts.stream()
                .anyMatch(d -> d.getAmount() == 1000 &&
                        d.getDebtor().getId() == memberC.getId() &&
                        d.getCreditor().getId() == memberA.getId()));

    }

    @Test
    public void shouldReturnNoDebtsWhenExpensesAreOpposite() {
        Member memberA = new Member();
        memberA.setId(1);
        Member memberB = new Member();
        memberB.setId(2);

        Expense expense1 = new Expense();
        expense1.setAmount(1000);
        expense1.setPayer(memberA);

        Involvement involvement1 = new Involvement();
        involvement1.setPayee(memberB);
        involvement1.setWeight(1);

        expense1.setInvolvements(Collections.singleton(involvement1));

        Expense expense2 = new Expense();
        expense2.setAmount(1000);
        expense2.setPayer(memberB);

        Involvement involvement2 = new Involvement();
        involvement2.setPayee(memberA);
        involvement2.setWeight(1);

        expense2.setInvolvements(Collections.singleton(involvement2));
        List<Debt> debts = debtService.calculateDebts(List.of(expense1, expense2));

        assertEquals(debts.size(), 0);
    }

    @Test
    public void shouldReturnNoDebtsWhenNoExpenses() {
        List<Expense> expenses = new ArrayList<>();
        List<Debt> debts = debtService.calculateDebts(expenses);

        assertEquals(debts.size(), 0);
    }

    @Test
    public void shouldReturnDebtsWithDifferentAmountsWhenWeightsIncluded() {
        Member memberA = new Member();
        memberA.setId(1);
        Member memberB = new Member();
        memberB.setId(2);
        Member memberC = new Member();
        memberC.setId(3);

        Expense expense = new Expense();
        expense.setAmount(3000);
        expense.setPayer(memberA);

        Involvement involvement1 = new Involvement();
        involvement1.setPayee(memberB);
        involvement1.setWeight(1);

        Involvement involvement2 = new Involvement();
        involvement2.setPayee(memberC);
        involvement2.setWeight(2);

        expense.setInvolvements(Set.of(involvement1, involvement2));
        List<Debt> debts = debtService.calculateDebts(List.of(expense));

        assertEquals(debts.size(), 2);
        assertTrue(debts.stream()
                .anyMatch(d -> d.getAmount() == 1000 && d.getDebtor().getId() == memberB.getId()));
        assertTrue(debts.stream()
                .anyMatch(d -> d.getAmount() == 2000 && d.getDebtor().getId() == memberC.getId()));
    }

}