package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.UserInput;
import com.example.groupexpensewebapp.dto.UserSummary;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.model.UserEntity;
import com.example.groupexpensewebapp.repository.PersonRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
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
    private final PersonRepository personRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = repository.findByName(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(user.getName(), user.getPassword(), Collections.emptyList());
    }

    public boolean checkIfUserExists(String username) {
        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return repository.existsByName(username);
    }

    public UserSummary addUser(UserInput input) {
        if (input.getName() == null || input.getPassword() == null || repository.existsByName(input.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setName(input.getName());
        String rawPassword = input.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        UserEntity savedUser = repository.save(user);
        return modelMapper.map(savedUser, UserSummary.class);
    }

    public List<UserSummary> findUsers(String query, String username) {
        if (query.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<UserEntity> users = repository.findByNameContainingIgnoreCase(query);

        return users.stream()
                .filter(user -> !user.getName().equals(username))
                .map(user -> modelMapper.map(user, UserSummary.class))
                .collect(Collectors.toList());
    }

    public UserSummary addUserFromInviteLink(long personId, UserInput input) {
        if (input.getName() == null || input.getPassword() == null || repository.existsByName(input.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity user = person.getRelatedUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        user.setName(input.getName());
        String rawPassword = input.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        repository.save(user);
        return modelMapper.map(user, UserSummary.class);
    }
}
