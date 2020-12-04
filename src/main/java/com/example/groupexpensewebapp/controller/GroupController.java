package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.*;
import com.example.groupexpensewebapp.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public List<GroupSummary> getGroupsForUser(Principal principal) {
        return groupService.getGroupsForUser(principal.getName());
    }

    @PostMapping
    public GroupSummary addGroup(@RequestBody GroupInput input, Principal principal) {
        return groupService.addGroup(input, principal.getName());
    }

    @PutMapping("/{id}")
    public GroupSummary editGroup(@PathVariable long id, @RequestBody GroupInput input, Principal principal) {
        return groupService.editGroup(id, input, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteGroup(@PathVariable long id, Principal principal) {
        groupService.deleteGroup(id, principal.getName());
    }

    @PutMapping("/{id}/archive")
    public void archiveGroup(@PathVariable long id, Principal principal) {
        this.groupService.archiveGroup(id, principal.getName());
    }

    @GetMapping("/{id}")
    public GroupDetails getGroupDetails(@PathVariable long id, Principal principal) {
        return groupService.getGroupDetails(id, principal != null ? principal.getName() : null);
    }

    @GetMapping("/{id}/debts")
    public List<Debt> getDebts(@PathVariable long id, Principal principal) {
        return groupService.getDebtsForGroup(id, principal != null ? principal.getName() : null);
    }

//    @PutMapping("/{id}/member")
//    public MemberSummary addGroupMember(@PathVariable long id, @RequestBody MemberInput memberInput, Principal principal) {
//        return groupService.addGroupMember(id, memberInput, principal != null ? principal.getName() : null);
//    }
//
//    @DeleteMapping("/{id}/member")
//    public void deleteGroupMember(@PathVariable long id, Principal principal) {
//        groupService.deleteGroupMember(id, principal != null ? principal.getName() : null);
//    }


}
