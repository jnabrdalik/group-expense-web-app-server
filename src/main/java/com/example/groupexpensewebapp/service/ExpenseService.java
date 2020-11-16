package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.ExpenseHistory;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.repository.ExpenseHistoryRepository;
import com.example.groupexpensewebapp.repository.ExpenseRepository;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.PersonRepository;
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

    private final ExpenseRepository repository;
    private final ExpenseHistoryRepository expenseHistoryRepository;
    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final ModelMapper modelMapper;

    public ExpenseDetails getExpenseDetails(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long groupId = expense.getGroup().getId();
        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    public List<ExpenseChange> getExpenseHistory(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Group group = expense.getGroup();
        if (group.isRegisteredOnly() && !personRepository.existsByRelatedUserName_AndGroup_Id(username, group.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        ExpenseSummary currentVersion = modelMapper.map(expense, ExpenseSummary.class);
        currentVersion.setVersionCreatedTime(expense.getCreatedTime());
        currentVersion.setVersionCreatedBy(modelMapper.map(expense.getCreatedBy(), PersonSummary.class));

        List<ExpenseSummary> history = Stream.concat(expense.getHistory().stream()
                .map(e -> modelMapper.map(e, ExpenseSummary.class)), Stream.of(currentVersion))
                .sorted(Comparator.comparingLong(ExpenseSummary::getVersionCreatedTime).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        List<ExpenseChange> changes = new ArrayList<>();
        for (int i = 0; i < history.size() - 1; i++) {
            ExpenseSummary previousVersion = history.get(i);
            ExpenseSummary nextVersion = history.get(i + 1);

            PersonSummary changedBy = modelMapper.map(nextVersion.getVersionCreatedBy(), PersonSummary.class);
            long changeTimestamp = nextVersion.getVersionCreatedTime();

            List<ExpenseChange.FieldChange> fieldChanges = new ArrayList<>();
            ExpenseChange expenseChange = new ExpenseChange(changedBy, changeTimestamp, fieldChanges);

            if (previousVersion.getAmount() != nextVersion.getAmount()) {
                fieldChanges.add(new ExpenseChange.FieldChange( "amount",
                        Integer.toString(previousVersion.getAmount()),
                        Integer.toString(nextVersion.getAmount())));
            }

            if (!previousVersion.getDescription().equals(nextVersion.getDescription())) {
                fieldChanges.add(new ExpenseChange.FieldChange("description",
                        previousVersion.getDescription(),
                        nextVersion.getDescription()));
            }

            if (previousVersion.getTimestamp() != nextVersion.getTimestamp()) {
                fieldChanges.add(new ExpenseChange.FieldChange("timestamp",
                        Long.toString(previousVersion.getTimestamp()),
                        Long.toString(nextVersion.getTimestamp())));
            }

            if (previousVersion.getPayer().getId() != nextVersion.getPayer().getId()) {
                fieldChanges.add(new ExpenseChange.FieldChange("payer",
                        previousVersion.getPayer().getName(),
                        nextVersion.getPayer().getName()));
            }

            if (!previousVersion.getPayees().equals(nextVersion.getPayees())) {
                fieldChanges.add(new ExpenseChange.FieldChange("payees",
                        getPayeesNames(previousVersion.getPayees()),
                        getPayeesNames(nextVersion.getPayees())));
            }

            changes.add(expenseChange);
        }

        return changes;
    }

    private String getPayeesNames(List<PersonSummary> payees) {
        StringBuilder sb = new StringBuilder();
        payees.stream()
                .map(PersonSummary::getName)
                .sorted()
                .forEachOrdered(n -> sb.append(n).append(", "));

        String concatenated = sb.toString();
        return concatenated.substring(0, concatenated.length() - 2);
    }

    public ExpenseDetails addExpense(ExpenseInput input, String username) {
        Person createdBy = personRepository.findByRelatedUserName_AndGroup_Id(username, input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        if (input.getDescription() == null || input.getAmount() < 0 || input.getPayeesIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Person payer = personRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        Expense expense = new Expense();
        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setTimestamp(input.getTimestamp());
        expense.setPayer(payer);
        expense.setPayees(getPayees(input));
        expense.setGroup(group);

        long currentTime = System.currentTimeMillis();
        expense.setCreatedTime(currentTime);
        expense.setCreatedBy(createdBy);
        expense.setLastEditTime(currentTime);
        expense.setLastEditedBy(createdBy);

        Expense addedExpense = repository.save(expense);
        return modelMapper.map(addedExpense, ExpenseDetails.class);
    }

    public ExpenseDetails editExpense(long expenseId, ExpenseInput input, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Group group = expense.getGroup();
        Person editedBy = personRepository.findByRelatedUserName_AndGroup_Id(username, group.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        Person payer = personRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (input.getDescription() == null || input.getAmount() < 0 || input.getPayeesIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        ExpenseHistory oldExpense = new ExpenseHistory();
        oldExpense.setExpense(expense);
        oldExpense.setDescription(expense.getDescription());
        oldExpense.setAmount(expense.getAmount());
        oldExpense.setTimestamp(expense.getTimestamp());
        oldExpense.setPayer(expense.getPayer());
        oldExpense.setPayees(Set.copyOf(expense.getPayees()));
        oldExpense.setVersionCreatedTime(expense.getLastEditTime());
        oldExpense.setVersionCreatedBy(expense.getLastEditedBy());
        expenseHistoryRepository.save(oldExpense);

        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setTimestamp(input.getTimestamp());
        expense.setPayer(payer);
        expense.setPayees(getPayees(input));
        expense.setLastEditTime(System.currentTimeMillis());
        expense.setLastEditedBy(editedBy);
        repository.save(expense);

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    private Set<Person> getPayees(ExpenseInput input) {
        Set<Person> peopleInvolved = new HashSet<>();
        Set<Long> peopleInvolvedIds = input.getPayeesIds();
        for (Person person : personRepository.findAllById(peopleInvolvedIds)) {
            peopleInvolved.add(person);
        }

        if (peopleInvolved.size() != peopleInvolvedIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, peopleInvolved.size() + " != " + peopleInvolvedIds.size());
        }

        return peopleInvolved;
    }

    public ExpenseDetails revertLastChange(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String lastEditorUsername = expense.getLastEditedBy().getRelatedUser().getName();
        String groupCreatorUsername = expense.getGroup().getCreator().getName();

        if (!username.equals(lastEditorUsername) && !username.equals(groupCreatorUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        ExpenseHistory previousVersion = expenseHistoryRepository.findFirstByExpense_IdOrderByTimestampDesc(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        expense.setDescription(previousVersion.getDescription());
        expense.setAmount(previousVersion.getAmount());
        expense.setTimestamp(previousVersion.getTimestamp());
        expense.setPayer(previousVersion.getPayer());
        expense.setPayees(new HashSet<>(previousVersion.getPayees()));
        expense.setLastEditedBy(previousVersion.getVersionCreatedBy());
        expense.setLastEditTime(previousVersion.getVersionCreatedTime());

        repository.save(expense);
        expenseHistoryRepository.delete(previousVersion);

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    public void deleteExpense(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!expense.getCreatedBy().getRelatedUser().getName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        repository.deleteById(expenseId);
    }

    private void checkIfExpenseExists(long expenseId) {

    }

}
