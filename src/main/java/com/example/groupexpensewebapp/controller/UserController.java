package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.UserInput;
import com.example.groupexpensewebapp.dto.UserSummary;
import com.example.groupexpensewebapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public UserSummary signUp(@RequestBody UserInput userInput) {
        return userService.addUser(userInput);
    }

    @GetMapping("/{username}/exists")
    public boolean checkIfUsernameTaken(@PathVariable String username) {
        return userService.checkIfUserExists(username);
    }

    @GetMapping("/{query}/find")
    public List<UserSummary> findUsers(@PathVariable String query, Principal principal) {
        return userService.findUsers(query, principal.getName());
    }

    @PutMapping("/password")
    public void changePassword(@RequestBody String newPassword, Principal principal) {
        userService.changePassword(newPassword, principal.getName());
    }
}
