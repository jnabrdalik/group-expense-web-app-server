package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.Debt;
import com.example.groupexpensewebapp.dto.MemberDetails;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Involvement;
import com.example.groupexpensewebapp.model.Member;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final ModelMapper modelMapper;

    public List<Debt> calculateDebts(Iterable<Expense> expenses) {
        List<Debt> debts = new ArrayList<>();

        HashMap<Member, HashMap<Member, Double>> balances = new HashMap<>();

        for (Expense expense : expenses) {
            Member payer = expense.getPayer();
            long payerId = payer.getId();;

            double amountPerWeightUnit = (double) expense.getAmount() / expense.getInvolvements().stream()
                    .mapToInt(Involvement::getWeight)
                    .sum();

            for (Involvement involvement : expense.getInvolvements()) {
                Member payee = involvement.getPayee();
                long payeeId = payee.getId();
                double amountDue = amountPerWeightUnit * involvement.getWeight();

                if (payeeId > payerId) {
                    balances.putIfAbsent(payer, new HashMap<>());

                    HashMap<Member, Double> currentPayerBalances = balances.get(payer);
                    currentPayerBalances.merge(payee, -amountDue, (previousBalance, a) -> previousBalance - amountDue);
                }
                else if (payerId > payeeId) {
                    balances.putIfAbsent(payee, new HashMap<>());

                    HashMap<Member, Double> currentPayeeBalances = balances.get(payee);
                    currentPayeeBalances.merge(payer, amountDue, (previousBalance, a) -> previousBalance + amountDue);
                }
            }
        }

        balances.forEach((firstMember, memberToBalanceMapping) -> {
            MemberDetails firstMemberDetails = modelMapper.map(firstMember, MemberDetails.class);
            memberToBalanceMapping.forEach((secondMember, balance) -> {
                MemberDetails secondMemberDetails = modelMapper.map(secondMember, MemberDetails.class);

                int roundedBalance = (int) Math.round(balance);
                if (roundedBalance > 0) {
                    debts.add(new Debt(firstMemberDetails, secondMemberDetails, roundedBalance));
                } else if (roundedBalance < 0) {
                    debts.add(new Debt(secondMemberDetails, firstMemberDetails, -roundedBalance));
                }
            });
        });

        return debts;
    }
}
