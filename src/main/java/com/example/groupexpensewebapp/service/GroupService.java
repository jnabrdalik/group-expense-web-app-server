package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.model.UserEntity;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.PersonRepository;
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
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
        if (!repository.existsByIdAndCreator_Name(groupId, username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getName() == null || input.getDescription() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        repository.deleteById(groupId);
    }

    public GroupDetails getGroupDetails(long groupId, String username) {
        Group group = repository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return modelMapper.map(group, GroupDetails.class);
    }

    public List<Debt> calculateDebtsForGroup(long groupId, String username) {
        Group group = repository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!personRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return debtService.calculateDebts(group.getExpenses());
    }

}
