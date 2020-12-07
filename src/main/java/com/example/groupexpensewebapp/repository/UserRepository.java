package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByName(String username);

    boolean existsByName(String username);

    List<User> findByNameContainingIgnoreCase(String pattern);
}
