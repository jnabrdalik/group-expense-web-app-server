package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.model.UserEntity;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.PersonRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository repository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final DebtService debtService;
    private final ModelMapper modelMapper;

    public List<GroupSummary> getGroupsForUser(String username) {
        return repository.findAllGroupsForUser(username).stream()
                .map(g -> modelMapper.map(g, GroupSummary.class))
                .collect(Collectors.toList());
    }

    public GroupSummary addGroup(GroupInput input, String creatorUsername) {
        if (input.getName() == null || input.getDescription() == null) {
            throw new IllegalArgumentException();
        }

        UserEntity creator = userRepository.findByName(creatorUsername);

        Group group = new Group();
        group.setName(input.getName());
        group.setDescription(input.getDescription());
        group.setTimeCreated(System.currentTimeMillis());
        group.setCreator(creator);

        Person person = new Person();
        person.setName(creatorUsername);
        person.setRelatedUser(creator);
        person.setGroup(group);
        group.setPersons(Collections.singletonList(person));

        Group addedGroup = repository.save(group);
        return modelMapper.map(addedGroup, GroupSummary.class);
    }

    public GroupSummary editGroup(long groupId, GroupInput input, String username) {
        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new IllegalArgumentException("brak praw do edycji"); // docelowo inny wyjątek
        }

        if (input.getName() == null || input.getDescription() == null) {
            throw new IllegalArgumentException();
        }

        Group group = repository.findById(groupId)
                .orElseThrow(IllegalArgumentException::new);
        group.setName(input.getName());
        group.setDescription(input.getDescription());

        repository.save(group);
        return modelMapper.map(group, GroupSummary.class);
    }

    public void deleteGroup(long groupId, String username) {
        if (!repository.existsByIdAndCreator_Name(groupId, username)) {
            throw new IllegalArgumentException("brak praw do usuwania"); // docelowo inny wyjątek
        }

        repository.deleteById(groupId);
    }

    public GroupDetails getGroupDetails(long groupId, String username) {
        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new IllegalArgumentException("brak praw do wyswietlania"); // docelowo inny wyjątek
        }

        Group group = repository.findById(groupId)
                .orElseThrow(IllegalArgumentException::new);

        return modelMapper.map(group, GroupDetails.class);
    }

    public Object calculateDebtsForGroup(long groupId) {
        Group group = repository.findById(groupId)
                .orElseThrow(IllegalArgumentException::new);
        Iterable<Expense> expenses = group.getExpenses();

        ModelMapper modelMapper = new ModelMapper();
        List<ExpenseDetails> expenseList = new ArrayList<>();
        for (Expense expense : expenses) {
            expenseList.add(modelMapper.map(expense, ExpenseDetails.class));
        }

        return debtService.calculateDebts(expenses);
        //return debtService.calculateBalances(expenses);
    }

}
