package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

    UserEntity findByName(String username);

    boolean existsByName(String username);

    List<UserEntity> findByNameContainingIgnoreCase(String pattern);
}
