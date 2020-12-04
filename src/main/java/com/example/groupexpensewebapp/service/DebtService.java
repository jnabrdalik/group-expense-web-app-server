package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.Debt;
import com.example.groupexpensewebapp.dto.MemberSummary;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Involvement;
import com.example.groupexpensewebapp.model.Member;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final ModelMapper modelMapper;

    public List<Debt> getDebts(Iterable<Expense> expenses) {
        List<Debt> debts = new ArrayList<>();

        HashMap<Member, HashMap<Member, Double>> balancesBetweenMembers = new HashMap<>();

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
                    balancesBetweenMembers.putIfAbsent(payer, new HashMap<>());

                    HashMap<Member, Double> currentPayeeDebts = balancesBetweenMembers.get(payer);
                    currentPayeeDebts.compute(payee, (k, v) -> v == null ? -amountDue : v - amountDue);
                }
                else if (payerId > payeeId) {
                    balancesBetweenMembers.putIfAbsent(payee, new HashMap<>());

                    HashMap<Member, Double> currentPersonDebts = balancesBetweenMembers.get(payee);
                    currentPersonDebts.compute(payer, (k, v) -> v == null ? amountDue : v + amountDue);
                }
            }
        }

        balancesBetweenMembers.forEach((firstMember, memberToBalanceMapping) -> {
            MemberSummary firstMemberSummary = modelMapper.map(firstMember, MemberSummary.class);
            memberToBalanceMapping.forEach((secondMember, balance) -> {
                MemberSummary secondMemberSummary = modelMapper.map(secondMember, MemberSummary.class);

                int roundedBalance = (int) Math.round(balance);
                if (roundedBalance > 0) {
                    debts.add(new Debt(firstMemberSummary, secondMemberSummary, roundedBalance));
                } else if (roundedBalance < 0) {
                    debts.add(new Debt(secondMemberSummary, firstMemberSummary, -roundedBalance));
                }
            });
        });

        return debts;
    }

    Map<Long, Integer> getBalances(Iterable<Expense> expenses) {
        HashMap<Long, Double> balances = new HashMap<>();
        for (Expense expense : expenses) {
            int amount = expense.getAmount();
            Set<Involvement> involvements = expense.getInvolvements();
            double amountPerWeightUnit = (double) amount / involvements.stream().mapToInt(Involvement::getWeight).sum();
            Member payer = expense.getPayer();

            int payerWeight = involvements.stream()
                    .filter(i -> i.getPayee().getId() == payer.getId())
                    .findAny()
                    .map(Involvement::getWeight)
                    .orElse(0);


            double amountPaidForOthers = amount - payerWeight * amountPerWeightUnit;

            balances.compute(payer.getId(), (k, v) -> v == null ? amountPaidForOthers : v + amountPaidForOthers);

            for (Involvement involvement : involvements) {
                Member payee = involvement.getPayee();
                if (payee.getId() != payer.getId())
                    balances.compute(payee.getId(),
                            (k, v) -> v == null
                                    ? - amountPerWeightUnit * involvement.getWeight()
                                    : v - amountPerWeightUnit * involvement.getWeight());
            }
        }

        return balances.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (int) Math.round(e.getValue())));
    }

    public List<Debt> getDebtsForGroup(Group group) {

        Graph<Member, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        group.getMembers().forEach(graph::addVertex);
        group.getExpenses().forEach(expense -> {
            Member payer = expense.getPayer();

            double amountPerWeightUnit = (double) expense.getAmount() / expense.getInvolvements().stream()
                    .mapToInt(Involvement::getWeight)
                    .sum();

            expense.getInvolvements().forEach(involvement -> {
                Member payee = involvement.getPayee();
                DefaultWeightedEdge edge = graph.getEdge(payee, payer);
                if (edge == null) {
                    edge = graph.addEdge(payee, payer);
                    graph.setEdgeWeight(edge, 0.0);
                }
                double currentAmount = graph.getEdgeWeight(edge);
                graph.setEdgeWeight(edge,currentAmount + amountPerWeightUnit * involvement.getWeight());

            });
        });


        return null;
    }
}
