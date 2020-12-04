package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.DebtNotificationInput;
import com.example.groupexpensewebapp.dto.UserInput;
import com.example.groupexpensewebapp.dto.UserSummary;
import com.example.groupexpensewebapp.model.Member;
import com.example.groupexpensewebapp.model.UserEntity;
import com.example.groupexpensewebapp.repository.MemberRepository;
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

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final MailService mailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByName(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(user.getName(), user.getPassword(), Collections.emptyList());
    }

    public boolean checkIfUserExists(String username) {
        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return userRepository.existsByName(username);
    }

    public UserSummary addUser(UserInput input) {
        if (input.getName() == null || input.getPassword() == null || input.getEmail() == null ||
                userRepository.existsByName(input.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setName(input.getName());
        user.setEmail(input.getEmail());
        String rawPassword = input.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        UserEntity savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserSummary.class);
    }

    public void changePassword(String newPassword, String username) {
        UserEntity user = userRepository.findByName(username);
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    public List<UserSummary> findUsers(String query, String username) {
        if (query.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<UserEntity> users = userRepository.findByNameContainingIgnoreCase(query);

        return users.stream()
                .filter(user -> !user.getName().equals(username))
                .map(user -> modelMapper.map(user, UserSummary.class))
                .collect(Collectors.toList());
    }
}
