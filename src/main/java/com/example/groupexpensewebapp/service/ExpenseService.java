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
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final ModelMapper modelMapper;

//    public ExpenseService(ExpenseRepository repository, GroupRepository groupRepository, PersonRepository personRepository, ModelMapper modelMapper) {
//        this.repository = repository;
//        this.groupRepository = groupRepository;
//        this.personRepository = personRepository;
//        this.modelMapper = modelMapper;
//    }

    public ExpenseDetails getExpenseDetails(long expenseId) {
        Expense expense = repository.findById(expenseId).orElse(null);

        return modelMapper.map(expense, ExpenseDetails.class);
    }

    public ExpenseDetails addExpense(ExpenseInput input) {
        if (input.getDescription() == null || input.getAmount() < 0 || input.getPeopleInvolvedIds() == null) {
            throw new IllegalArgumentException();
        }

        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(IllegalArgumentException::new);
        Person payer = personRepository.findById(input.getPayerId())
                .orElseThrow(IllegalArgumentException::new);

        Set<Person> peopleInvolved = getPeopleInvolved(input);

        Expense expense = new Expense();
        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        expense.setGroup(group);
        expense.setPayer(payer);
        long currentTime = System.currentTimeMillis();
        expense.setTimeAdded(currentTime);
        expense.setPeopleInvolved(peopleInvolved);

        Expense addedExpense = repository.save(expense);
        return modelMapper.map(addedExpense, ExpenseDetails.class);
    }

    public ExpenseDetails editExpense(long expenseId, ExpenseInput input) {
        if (input.getDescription() == null || input.getAmount() < 0 || input.getPeopleInvolvedIds() == null) {
            throw new IllegalArgumentException();
        }

        Expense expense = repository.findById(expenseId)
                .orElseThrow(IllegalArgumentException::new);


        if (expense.getPayer().getId() != input.getPayerId()) {
            Person payer = personRepository.findById(input.getPayerId())
                    .orElseThrow(IllegalArgumentException::new);
            expense.setPayer(payer);
        }

        expense.setDescription(input.getDescription());
        expense.setAmount(input.getAmount());
        Set<Person> peopleInvolved = getPeopleInvolved(input);
        expense.setPeopleInvolved(peopleInvolved);
        expense.setTimeAdded(System.currentTimeMillis());

        repository.save(expense);
        return modelMapper.map(expense, ExpenseDetails.class);
    }

    private Set<Person> getPeopleInvolved(ExpenseInput input) {
        Set<Person> peopleInvolved = new HashSet<>();
        Set<Long> peopleInvolvedIds = input.getPeopleInvolvedIds();
        for (Person person : personRepository.findAllById(peopleInvolvedIds)) {
            peopleInvolved.add(person);
        }

        if (peopleInvolved.size() != peopleInvolvedIds.size()) {
            throw new IllegalArgumentException();
        }

        return peopleInvolved;
    }

    public void deleteExpense(long id) {
        repository.deleteById(id);
    }

}
