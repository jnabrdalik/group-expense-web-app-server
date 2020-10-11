package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

//    @Query(value = "select g. from groups g join persons p on g  ", nativeQuery = true)
//    List<Group> findALlGroupsForUser(long userId)
}
