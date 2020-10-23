package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.UserInput;
import com.example.groupexpensewebapp.dto.UserSummary;
import com.example.groupexpensewebapp.model.UserEntity;
import com.example.groupexpensewebapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
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
        return repository.existsByName(username);
    }

    public void addUser(UserInput input) {
        if (repository.existsByName(input.getName())) {
            throw new IllegalArgumentException();
        }

        UserEntity user = new UserEntity();
        user.setName(input.getName());
        String rawPassword = input.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        repository.save(user);
    }

    public List<UserSummary> findUsers(String query) {
        if (query.length() < 3) {
            throw new IllegalArgumentException();
        }

        List<UserEntity> users = repository.findByNameContainingIgnoreCase(query);

        return users.stream()
                .map(user -> modelMapper.map(user, UserSummary.class))
                .collect(Collectors.toList());
    }
}