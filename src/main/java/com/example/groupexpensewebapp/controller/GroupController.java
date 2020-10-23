package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.GroupDetails;
import com.example.groupexpensewebapp.dto.GroupInput;
import com.example.groupexpensewebapp.dto.GroupSummary;
import com.example.groupexpensewebapp.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

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
    public Object getDebts(@PathVariable long id) {
        return groupService.calculateDebtsForGroup(id);
    }

    @GetMapping("/test")
    public Object test(Principal principal) {
        return principal.getClass();
    }

}
