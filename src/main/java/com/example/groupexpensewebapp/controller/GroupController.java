package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.Debt;
import com.example.groupexpensewebapp.dto.GroupDetails;
import com.example.groupexpensewebapp.dto.GroupInput;
import com.example.groupexpensewebapp.dto.GroupSummary;
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

    @GetMapping("/{id}")
    public GroupDetails getGroupDetails(@PathVariable long id, Principal principal) {
        return groupService.getGroupDetails(id, principal.getName());
    }

    @GetMapping("/{id}/debts")
    public List<Debt> getDebts(@PathVariable long id, Principal principal) {
        return groupService.calculateDebtsForGroup(id, principal.getName());
    }

    @GetMapping("/test")
    public Object test(Principal principal) {
        return principal.getClass();
    }

}
