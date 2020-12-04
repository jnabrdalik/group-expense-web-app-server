package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Member;
import com.example.groupexpensewebapp.model.UserEntity;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DebtService debtService;
    private final ModelMapper modelMapper;

    public List<GroupSummary> getGroupsForUser(String username) {
        return groupRepository.findAllGroupsForUser(username).stream()
                .map(g -> modelMapper.map(g, GroupSummary.class))
                .collect(Collectors.toList());
    }

    public GroupSummary addGroup(GroupInput input, String creatorUsername) {
        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        UserEntity creator = userRepository.findByName(creatorUsername);

        Group group = new Group();
        group.setName(input.getName());
        group.setForRegisteredOnly(input.isForRegisteredOnly());
        group.setTimeCreated(System.currentTimeMillis());
        group.setCreator(creator);

        Member member = new Member();
        member.setName(creatorUsername);
        member.setRelatedUser(creator);
        member.setGroup(group);
        group.setMembers(Collections.singletonList(member));

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

        if (group.isForRegisteredOnly() && !memberRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }


        //        Map<Long, Integer> balances = debtService.getBalances(group.getExpenses());
//        groupDetails.getMembers().forEach(person ->
//                person.setBalance(balances.getOrDefault(person.getId(), 0)));

        return modelMapper.map(group, GroupDetails.class);
    }

    public List<Debt> getDebtsForGroup(long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (group.isForRegisteredOnly() && !memberRepository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return debtService.getDebts(group.getExpenses());
    }

    public void archiveGroup(long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!group.getCreator().getName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        group.setArchived(true);

        groupRepository.save(group);
    }
}
