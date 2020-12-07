package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.debt.Debt;
import com.example.groupexpensewebapp.debt.DebtCalculator;
import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.User;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final DebtCalculator debtCalculator;
    private final ModelMapper modelMapper;

    public List<GroupSummary> getGroupsForUser(String username) {
        return userRepository.findByName(username).getGroups().stream()
                .map(g -> modelMapper.map(g, GroupSummary.class))
                .collect(Collectors.toList());
    }

    public GroupSummary addGroup(GroupInput input, String creatorUsername) {
        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User creator = userRepository.findByName(creatorUsername);

        Group group = new Group();
        group.setName(input.getName());
        group.setForRegisteredOnly(input.isForRegisteredOnly());
        group.setTimeCreated(System.currentTimeMillis());
        group.setCreator(creator);
        group.setUsers(Collections.singleton(creator));

        Group addedGroup = groupRepository.save(group);
        return modelMapper.map(addedGroup, GroupSummary.class);
    }

    public GroupSummary editGroup(long groupId, GroupInput input, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!group.getCreator().getName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        group.setName(input.getName());
        group.setForRegisteredOnly(input.isForRegisteredOnly());

        groupRepository.save(group);
        return modelMapper.map(group, GroupSummary.class);
    }

    public void deleteGroup(long groupId, String username) {
        if (!groupRepository.existsByIdAndCreator_Name(groupId, username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        groupRepository.deleteById(groupId);
    }

    public GroupDetails getGroupDetails(long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (group.isForRegisteredOnly() && group.getUsers().stream()
                .noneMatch(u -> u.getName().equals(username))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        GroupDetails groupDetails = modelMapper.map(group, GroupDetails.class);
        List<ExpenseDetails> expenses = new ArrayList<>();
        List<DebtPayment> payments = new ArrayList<>();
        group.getExpenses().forEach(expense -> {
            UserSummary creator = modelMapper.map(expense.getCreator(), UserSummary.class);
            UserSummary payer = modelMapper.map(expense.getPayer(), UserSummary.class);

            if (expense.getDescription() != null) {
                ExpenseDetails expenseDetails = modelMapper.map(expense, ExpenseDetails.class);
                Set<UserSummary> payees = expense.getPayees().stream()
                        .map(payee -> modelMapper.map(payee, UserSummary.class))
                        .collect(Collectors.toSet());
                expenseDetails.setCreator(creator);
                expenseDetails.setPayer(payer);
                expenseDetails.setPayees(payees);
                expenses.add(expenseDetails);
            }
            else {
                DebtPayment payment = modelMapper.map(expense, DebtPayment.class);
                UserSummary payee = modelMapper.map(expense.getPayees().stream()
                        .findAny().orElse(null), UserSummary.class);
                payment.setPayer(payer);
                payment.setPayee(payee);
                payments.add(payment);
            }
        });

        Map<Long, Integer> balances = getBalances(group.getExpenses());
        List<UserDetails> users = group.getUsers().stream()
                .map(user -> modelMapper.map(user, UserDetails.class))
                .collect(Collectors.toList());
        users.forEach(person ->
                person.setBalance(balances.getOrDefault(person.getId(), 0)));

        groupDetails.setExpenses(expenses);
        groupDetails.setUsers(users);
        groupDetails.setPayments(payments);

        return groupDetails;
    }

    private Map<Long, Integer> getBalances(Iterable<Expense> expenses) {
        HashMap<Long, Double> balances = new HashMap<>();
        for (Expense expense : expenses) {
            int amount = expense.getAmount();
            Set<User> payees = expense.getPayees();
            double amountPerUserEntity = (double) amount / payees.size();
            User payer = expense.getPayer();

            double amountPaidForOthers = payees.contains(payer) ? amount - amountPerUserEntity : amount;
            balances.compute(payer.getId(), (k, v) -> v == null ? amountPaidForOthers : v + amountPaidForOthers);

            for (User payee : payees) {
                if (payee.getId() != payer.getId())
                    balances.compute(payee.getId(), (k, v) -> v == null ? - amountPerUserEntity : v - amountPerUserEntity);
            }
        }

        return balances.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e -> (int) Math.round(e.getValue())));
    }

    public List<Debt> getDebtsForGroup(long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (group.isForRegisteredOnly() && group.getUsers().stream()
                .noneMatch(u -> u.getName().equals(username))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return debtCalculator.calculateDebts(group.getExpenses());
    }

    public UserSummary addUserToGroup(long userId, long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (group.getUsers().stream().noneMatch(u -> u.getName().equals(username))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't belong to this group!");
        }

        Set<User> users = new HashSet<>(group.getUsers());
        users.add(user);
        group.setUsers(users);

        groupRepository.save(group);

        return modelMapper.map(user, UserSummary.class);
    }

    public void deleteUserFromGroup(long userId, long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!group.getCreator().getName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (cannotDeleteUserFromGroup(userId, group)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Set<User> users = group.getUsers().stream()
                .filter(u -> u.getId() != userId)
                .collect(Collectors.toSet());
        group.setUsers(users);

        groupRepository.save(group);
    }

    private boolean cannotDeleteUserFromGroup(long userId, Group group) {
        return group.getCreator().getId() == userId || group.getExpenses().stream()
                .anyMatch(e -> e.getCreator().getId() == userId || e.getPayees().stream()
                        .anyMatch(u -> u.getId() == userId));
    }

}
