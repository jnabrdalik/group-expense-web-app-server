package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.GroupDetails;
import com.example.groupexpensewebapp.dto.GroupInput;
import com.example.groupexpensewebapp.dto.GroupSummary;
import com.example.groupexpensewebapp.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<GroupSummary> getAllGroups() {
        return groupService.getGroupsForUser(0);
    }

    @PostMapping
    public GroupSummary addGroup(@RequestBody GroupInput input) {
        return groupService.addGroup(input);
    }

    @PutMapping("/{id}")
    public GroupSummary editGroup(@PathVariable long id, @RequestBody GroupInput input) {
        return groupService.editGroup(id, input);
    }

    @DeleteMapping("/{id}")
    public void deleteGroup(@PathVariable long id) {
        groupService.deleteGroup(id);
    }

    @GetMapping("/{id}")
    public GroupDetails getGroupDetails(@PathVariable long id) {
        return groupService.getGroupDetails(id);
    }

    @GetMapping("/{id}/debts")
    public Object getDebts(@PathVariable long id) {
        return groupService.calculateDebtsForGroup(id);
    }

}
