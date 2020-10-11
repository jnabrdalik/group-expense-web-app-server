package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.Expense;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.repository.ExpenseRepository;
import com.example.groupexpensewebapp.repository.GroupRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {

    private final GroupRepository repository;
    private final ExpenseRepository expenseRepository;
    private final DebtService debtService;

    public GroupService(GroupRepository repository, ExpenseRepository expenseRepository, DebtService debtService) {
        this.repository = repository;
        this.expenseRepository = expenseRepository;
        this.debtService = debtService;
    }

    public List<GroupSummary> getGroupsForUser(long userId) {
        List<GroupSummary> groupSummaries = new ArrayList<>();

        for (Group g : repository.findAll()) {
            groupSummaries.add(mapToGroupSummary(g));
        }

        return groupSummaries;
    }

    public GroupSummary addGroup(GroupInput input) {
        if (input.getName() == null || input.getDescription() == null) {
            throw new IllegalArgumentException();
        }

        Group group = new Group();
        group.setName(input.getName());
        group.setDescription(input.getDescription());
        long currentTime = System.currentTimeMillis();
        group.setTimeCreated(currentTime);

        Group addedGroup = repository.save(group);
        return mapToGroupSummary(addedGroup);
    }

    public GroupSummary editGroup(long groupId, GroupInput input) {
        if (input.getName() == null || input.getDescription() == null) {
            throw new IllegalArgumentException();
        }

        Group group = repository.findById(groupId)
                .orElseThrow(IllegalArgumentException::new);
        group.setName(input.getName());
        group.setDescription(input.getDescription());

        repository.save(group);
        return mapToGroupSummary(group);
    }

    public void deleteGroup(long groupId) {
        repository.deleteById(groupId);
    }

    public GroupDetails getGroupDetails(long groupId) {
        Group group = repository.findById(groupId)
                .orElseThrow(IllegalArgumentException::new);
        return mapToGroupDetails(group);
    }

    public Object calculateDebtsForGroup(long groupId) {
        Iterable<Expense> expenses = expenseRepository.findAllByGroup_Id(groupId);
        ModelMapper modelMapper = new ModelMapper();
        List<ExpenseDetails> expenseList = new ArrayList<>();
        for (Expense expense : expenses) {
            expenseList.add(modelMapper.map(expense, ExpenseDetails.class));
        }

        return debtService.calculateDebts(expenses);
        //return debtService.calculateBalances(expenses);
    }

    private GroupSummary mapToGroupSummary(Group group) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(group, GroupSummary.class);
    }

    private GroupDetails mapToGroupDetails(Group group) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(group, GroupDetails.class);
    }
}
