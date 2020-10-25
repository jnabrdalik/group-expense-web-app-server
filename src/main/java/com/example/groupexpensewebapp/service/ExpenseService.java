package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.ExpenseDetails;
import com.example.groupexpensewebapp.dto.ExpenseInput;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.repository.ExpenseRepository;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
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

    public ExpenseDetails addExpense(ExpenseInput input, String username) {
        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, input.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getDescription() == null || input.getAmount() < 0 || input.getPeopleInvolvedIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Person payer = personRepository.findById(input.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        List<Person> peopleInvolved = getPeopleInvolved(input);

        Expense expense = new Expense();
        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setGroup(group);
        expense.setPayer(payer);
        expense.setTimeAdded(System.currentTimeMillis());
        expense.setPeopleInvolved(peopleInvolved);

        Expense addedExpense = repository.save(expense);
        return modelMapper.map(addedExpense, ExpenseDetails.class);
    }

    public ExpenseDetails editExpense(long expenseId, ExpenseInput input, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long groupId = expense.getGroup().getId();
        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getDescription() == null || input.getAmount() < 0 || input.getPeopleInvolvedIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (expense.getPayer().getId() != input.getPayerId()) {
            Person payer = personRepository.findById(input.getPayerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
            expense.setPayer(payer);
        }

        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        List<Person> peopleInvolved = getPeopleInvolved(input);
        expense.setPeopleInvolved(peopleInvolved);
        expense.setTimeAdded(System.currentTimeMillis());

        repository.save(expense);
        return modelMapper.map(expense, ExpenseDetails.class);
    }

    private List<Person> getPeopleInvolved(ExpenseInput input) {
        personRepository.findAllById(input.getPeopleInvolvedIds());

        List<Person> peopleInvolved = new ArrayList<>();
        List<Long> peopleInvolvedIds = input.getPeopleInvolvedIds();
        for (Person person : personRepository.findAllById(peopleInvolvedIds)) {
            peopleInvolved.add(person);
        }

        if (peopleInvolved.size() != peopleInvolvedIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return peopleInvolved;
    }

    public void deleteExpense(long expenseId, String username) {
        Expense expense = repository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long groupId = expense.getGroup().getId();
        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        repository.deleteById(expenseId);
    }

}
