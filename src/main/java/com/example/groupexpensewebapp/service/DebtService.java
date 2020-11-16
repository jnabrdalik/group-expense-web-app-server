package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.Debt;
import com.example.groupexpensewebapp.dto.PersonSummary;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Person;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class DebtService {

    private final ModelMapper modelMapper;

    List<Debt> getDebts(Iterable<Expense> expenses) {
        List<Debt> debts = new ArrayList<>();

        HashMap<Person, HashMap<Person, Integer>> personIdsToAmountMapping = new HashMap<>();

        for (Expense expense : expenses) {
            Person payer = expense.getPayer();
            long payerId = payer.getId();
            int amountPerPerson = expense.getAmount() / expense.getPayees().size();

            for (Person personInvolved : expense.getPayees()) {

                long personInvolvedId = personInvolved.getId();

                if (personInvolvedId > payerId) {
                    personIdsToAmountMapping.putIfAbsent(payer, new HashMap<>());

                    HashMap<Person, Integer> currentPersonDebts = personIdsToAmountMapping.get(payer);
                    currentPersonDebts.compute(personInvolved, (k, v) -> v == null ? -amountPerPerson : v - amountPerPerson);
                }
                else if (payerId > personInvolvedId) {
                    personIdsToAmountMapping.putIfAbsent(personInvolved, new HashMap<>());

                    HashMap<Person, Integer> currentPersonDebts = personIdsToAmountMapping.get(personInvolved);
                    currentPersonDebts.compute(payer, (k, v) -> v == null ? amountPerPerson : v + amountPerPerson);
                }
            }
        }

        for (Map.Entry<Person, HashMap<Person, Integer>> debtorEntry : personIdsToAmountMapping.entrySet()) {
            Person debtor = debtorEntry.getKey();
            PersonSummary debtorSummary = modelMapper.map(debtor, PersonSummary.class);
            for (Map.Entry<Person, Integer> creditorEntry : debtorEntry.getValue().entrySet()) {
                Person creditor = creditorEntry.getKey();
                PersonSummary creditorSummary = modelMapper.map(creditor, PersonSummary.class);

                int amount = creditorEntry.getValue();
                if (amount > 0) {
                    debts.add(new Debt(debtorSummary, creditorSummary, amount));
                }
                else if (amount < 0) {
                    debts.add(new Debt(creditorSummary, debtorSummary, -amount));
                }
            }
        }

        return debts;
    }

    Map<Long, Integer> getBalances(Iterable<Expense> expenses) {
        HashMap<Long, Integer> balances = new HashMap<>();
        for (Expense expense : expenses) {
            int amount = expense.getAmount();
            Set<Person> payees = expense.getPayees();
            int amountPerPerson = amount / payees.size();
            Person payer = expense.getPayer();

            int amountPaidForOthers = payees.contains(payer) ? amount - amountPerPerson : amount;
            balances.compute(payer.getId(), (k, v) -> v == null ? amountPaidForOthers : v + amountPaidForOthers);

            for (Person payee : payees) {
                if (payee.getId() != payer.getId())
                    balances.compute(payee.getId(), (k, v) -> v == null ? - amountPerPerson : v - amountPerPerson);
            }
        }

        return balances;
    }
}
