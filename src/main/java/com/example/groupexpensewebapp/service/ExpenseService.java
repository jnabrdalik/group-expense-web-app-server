package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.User;
import com.example.groupexpensewebapp.repository.ExpenseRepository;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<ExpenseChange> getExpenseHistory(long expenseId, String username) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense doesn't exist!"));

        Group group = expense.getGroup();
        if (group.isForRegisteredOnly() && group.getUsers().stream()
                .noneMatch(u -> u.getName().equals(username))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't belong to this group!");
        }

        List<ExpenseDetails> history = Stream.concat(expense.getPreviousVersions().stream(), Stream.of(expense))
                .map(e -> modelMapper.map(e, ExpenseDetails.class))
                .sorted(Comparator.comparingLong(ExpenseDetails::getTimeCreated))
                .collect(Collectors.toCollection(ArrayList::new));

        List<ExpenseChange> changes = new ArrayList<>();
        for (int i = 0; i < history.size() - 1; i++) {
            ExpenseDetails beforeChange = history.get(i);
            ExpenseDetails afterChange = history.get(i + 1);

            UserSummary changedBy = modelMapper.map(afterChange.getCreator(), UserSummary.class);
            long changeTimestamp = afterChange.getTimeCreated();

            List<ExpenseChange.FieldChange> fieldChanges = new ArrayList<>();
            ExpenseChange expenseChange = new ExpenseChange(changedBy, changeTimestamp, fieldChanges);

            if (beforeChange.getAmount() != afterChange.getAmount()) {
                fieldChanges.add(new ExpenseChange.FieldChange( "amount",
                        Integer.toString(beforeChange.getAmount()),
                        Integer.toString(afterChange.getAmount())));
            }

            if (!beforeChange.getDescription().equals(afterChange.getDescription())) {
                fieldChanges.add(new ExpenseChange.FieldChange("description",
                        beforeChange.getDescription(),
                        afterChange.getDescription()));
            }

            if (beforeChange.getTimestamp() != afterChange.getTimestamp()) {
                fieldChanges.add(new ExpenseChange.FieldChange("timestamp",
                        Long.toString(beforeChange.getTimestamp()),
                        Long.toString(afterChange.getTimestamp())));
            }

            if (beforeChange.getPayer().getId() != afterChange.getPayer().getId()) {
                fieldChanges.add(new ExpenseChange.FieldChange("payer",
                        beforeChange.getPayer().getName(),
                        afterChange.getPayer().getName()));
            }

            if (!beforeChange.getPayees().equals(afterChange.getPayees())) {
                fieldChanges.add(new ExpenseChange.FieldChange("payees",
                        getPayeesNames(beforeChange.getPayees()),
                        getPayeesNames(afterChange.getPayees())));
            }

            changes.add(expenseChange);
        }

        return changes;
    }

    private String getPayeesNames(Set<UserSummary> payees) {
        StringBuilder sb = new StringBuilder();
        payees.stream()
                .map(UserSummary::getName)
                .sorted()
                .forEachOrdered(n -> sb.append(n).append(", "));

        String concatenated = sb.toString();
        return concatenated.substring(0, concatenated.length() - 2);
    }

    public ExpenseDetails addExpense(ExpenseInput input, String username) {
        if (input.getDescription() == null || input.getAmount() < 0 || input.getPayeesIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group doesn't exist!"));
        User payer = userRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payer doesn't exist"));

        User creator = userRepository.findByName(username);
        if (group.isForRegisteredOnly() && group.getUsers().stream()
                .noneMatch(u -> u.getName().equals(username))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Expense expense = new Expense();
        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setTimestamp(input.getTimestamp());
        expense.setPayer(payer);
        expense.setPayees(getPayees(input));
        expense.setGroup(group);
        expense.setTimeCreated(System.currentTimeMillis());
        expense.setCreator(creator);

        Expense addedExpense = expenseRepository.save(expense);
        return modelMapper.map(addedExpense, ExpenseDetails.class);
    }

    public DebtPayment addPayment(DebtPaymentInput input, String username) {
        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group doesn't exist!"));

        if (group.isForRegisteredOnly() && group.getUsers().stream()
                .noneMatch(u -> u.getName().equals(username))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User creator = username != null ? userRepository.findByName(username) : null;
        User payer = userRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payer doesn't exist!"));
        User payee = userRepository.findById(input.getPayeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payee doesn't exist!"));

        Expense expense = new Expense();
        expense.setAmount(input.getAmount());
        expense.setCreator(creator);
        expense.setPayer(payer);
        expense.setPayees(Collections.singleton(payee));
        expense.setGroup(group);
        expense.setTimeCreated(System.currentTimeMillis());

        Expense savedExpense = expenseRepository.save(expense);
        DebtPayment payment = modelMapper.map(savedExpense, DebtPayment.class);
        payment.setPayer(modelMapper.map(payer, UserSummary.class));
        payment.setPayee(modelMapper.map(payee, UserSummary.class));

        return payment;
    }

    public ExpenseDetails editExpense(long expenseId, ExpenseInput input, String username) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense doesn't exist!"));
        
        if (input.getDescription() == null || input.getAmount() < 0 || input.getPayeesIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User payer = userRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        Group group = expense.getGroup();
        if (group.isForRegisteredOnly() && group.getUsers().stream()
                .noneMatch(u -> u.getName().equals(username)) || expense.getCreator() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User creator = userRepository.findByName(username);

        Expense oldExpense = new Expense();
        oldExpense.setOriginalExpense(expense);
        oldExpense.setDescription(expense.getDescription());
        oldExpense.setAmount(expense.getAmount());
        oldExpense.setTimestamp(expense.getTimestamp());
        oldExpense.setPayer(expense.getPayer());
        oldExpense.setPayees(Set.copyOf(expense.getPayees()));
        oldExpense.setCreator(expense.getCreator());
        oldExpense.setTimeCreated(expense.getTimeCreated());
        oldExpense.setGroup(null);
        expenseRepository.save(oldExpense);

        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setTimestamp(input.getTimestamp());
        expense.setTimeCreated(System.currentTimeMillis());
        expense.setPayer(payer);
        expense.setPayees(getPayees(input));
        expense.setCreator(creator);
        expenseRepository.save(expense);

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    private Set<User> getPayees(ExpenseInput input) {
        Set<User> payees = new HashSet<>();
        Set<Long> payeesIds = input.getPayeesIds();
        for (User user : userRepository.findAllById(payeesIds)) {
            payees.add(user);
        }

        if (payees.size() != payeesIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more payees don't exist!");
        }

        return payees;
    }

    public ExpenseDetails revertLastChange(long expenseId, String username) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense doesn't exist!"));

        String lastEditorUsername = expense.getCreator().getName();
        String groupCreatorUsername = expense.getGroup().getCreator().getName();

        if (expense.getGroup().isForRegisteredOnly() &&
                !username.equals(lastEditorUsername) &&
                !username.equals(groupCreatorUsername) || expense.getCreator() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only last editor or group creator can revert changes!");
        }

        Expense previousVersion = expense.getPreviousVersions().stream()
                .max(Comparator.comparingLong(Expense::getTimeCreated))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No previous versions exist!"));

        expense.setDescription(previousVersion.getDescription());
        expense.setAmount(previousVersion.getAmount());
        expense.setTimestamp(previousVersion.getTimestamp());
        expense.setPayer(previousVersion.getPayer());
        expense.setPayees(new HashSet<>(previousVersion.getPayees()));
        expense.setCreator(previousVersion.getCreator());
        expense.setTimeCreated(previousVersion.getTimeCreated());
        List<Expense> previousVersions = expense.getPreviousVersions().stream()
                .filter(e -> e.getId() != previousVersion.getId())
                .collect(Collectors.toList());
        expense.setPreviousVersions(previousVersions);

        expenseRepository.save(expense);
        expenseRepository.delete(previousVersion);

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    public void deleteExpense(long expenseId, String username) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense doesn't exist!"));

        Group group = expense.getGroup();
        if (group.isForRegisteredOnly() && expense.getCreator() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        
        String groupCreatorUsername = group.getCreator().getName();
        String originalCreatorName = expense.getPreviousVersions().stream()
                .min(Comparator.comparingLong(Expense::getTimeCreated))
                .orElse(expense)
                .getCreator()
                .getName();

        if (!groupCreatorUsername.equals(username) && !originalCreatorName.equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only original creator or group creator can delete an expense!");
        }

        expenseRepository.deleteById(expenseId);
    }
}
