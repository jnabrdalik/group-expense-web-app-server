package com.example.groupexpensewebapp.debt;

import com.example.groupexpensewebapp.dto.UserSummary;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DebtCalculatorImpl implements DebtCalculator {

    private final ModelMapper modelMapper;

    @Override
    public List<Debt> calculateDebts(Iterable<Expense> expenses) {
        List<Debt> debts = new ArrayList<>();

        HashMap<User, HashMap<User, Double>> userIdsToAmountMapping = new HashMap<>();

        for (Expense expense : expenses) {
            User payer = expense.getPayer();
            long payerId = payer.getId();
            double amountPerUserEntity = (double) expense.getAmount() / expense.getPayees().size();

            for (User payee : expense.getPayees()) {

                long payeeId = payee.getId();

                if (payeeId > payerId) {
                    userIdsToAmountMapping.putIfAbsent(payer, new HashMap<>());

                    HashMap<User, Double> currentUserEntityDebts = userIdsToAmountMapping.get(payer);
                    currentUserEntityDebts.compute(payee, (key, value) -> value == null ? -amountPerUserEntity : value - amountPerUserEntity);
                }
                else if (payerId > payeeId) {
                    userIdsToAmountMapping.putIfAbsent(payee, new HashMap<>());

                    HashMap<User, Double> currentUserEntityDebts = userIdsToAmountMapping.get(payee);
                    currentUserEntityDebts.compute(payer, (key, value) -> value == null ? amountPerUserEntity : value + amountPerUserEntity);
                }
            }
        }

        for (Map.Entry<User, HashMap<User, Double>> debtorEntry : userIdsToAmountMapping.entrySet()) {
            User debtor = debtorEntry.getKey();
            UserSummary debtorSummary = modelMapper.map(debtor, UserSummary.class);
            for (Map.Entry<User, Double> creditorEntry : debtorEntry.getValue().entrySet()) {
                User creditor = creditorEntry.getKey();
                UserSummary creditorSummary = modelMapper.map(creditor, UserSummary.class);

                int amount = (int) Math.round(creditorEntry.getValue());
                if (amount > 0) {
                    debts.add(new Debt(debtorSummary, creditorSummary, Math.round(amount)));
                }
                else if (amount < 0) {
                    debts.add(new Debt(creditorSummary, debtorSummary, -Math.round(amount)));
                }
            }
        }

        return debts;
    }
}
