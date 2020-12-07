package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.User;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, Long> {

    boolean existsByIdAndCreator_Name(long id, String creatorName);

    boolean existsByIdAndUsersContaining(long id, User user);

}
