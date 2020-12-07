package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.UserInput;
import com.example.groupexpensewebapp.dto.UserSummary;
import com.example.groupexpensewebapp.model.User;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final GroupRepository groupRepository;
    private final MailService mailService;
    private final ExpenseService expenseService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByName(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(), Collections.emptyList());
    }

    public boolean checkIfUserExists(String username) {
        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return repository.existsByName(username);
    }

    public UserSummary addUser(UserInput input) {
        if (input.getName() == null && input.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setName(input.getName());
        String rawPassword = input.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        User savedUser = repository.save(user);
        return modelMapper.map(savedUser, UserSummary.class);
    }

//    public UserSummary addUnregisteredUser(UserInput input) {
//
//    }

    public List<UserSummary> findUsers(String query, String username) {
        if (query.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<User> users = repository.findByNameContainingIgnoreCase(query);

        return users.stream()
                .filter(user -> !user.getName().equals(username))
                .map(user -> modelMapper.map(user, UserSummary.class))
                .collect(Collectors.toList());
    }

//    public void sendInvite(long userId, String email, String username) {
//        if (!mailService.isEmailValid(email)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//
//        User user = repository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//
//        user.getGroups().stream()
//                .anyMatch(g -> g.getUsers())
//
//        User user = new User();
//        userRepository.save(user);
//
//        person.setRelatedUser(user);
//        repository.save(person);
//
//        mailService.sendMessage(email, "Zaproszenie do aplikacji", "Cześć, " + person.getName() +
//                "! Zapraszam cię do grupy " + person.getGroup().getName() + ". Aby się zarejestrować, kliknij " +
//                "w poniższy link:\n" + "http://localhost:4200/sign-up/invite/" + userId);
//    }

    public UserSummary addUserFromInviteLink(long userId, UserInput input) {
        if (input.getName() == null || input.getPassword() == null || repository.existsByName(input.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User user = repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        user.setName(input.getName());
        String rawPassword = input.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        repository.save(user);
        return modelMapper.map(user, UserSummary.class);
    }

    public void sendDebtNotification(long userId, long groupId, String username) {
        User userToNotify = repository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        User currentUser = repository.findByName(username);

        mailService.sendMessage(userToNotify.getEmail(), "Powiadomienie o długu w aplikacji",
                "Cześć, " + userToNotify.getName() + ". " + currentUser.getName() +
                        " wysłał(a) ci powiadomienie o należności do spłacenia. Sprawdź w aplikacji:\n" +
                        "http://localhost:4200/groups/" + groupId);
    }
}
