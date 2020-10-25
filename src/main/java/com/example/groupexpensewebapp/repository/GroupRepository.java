package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

    @Query(value =
            "select g from Group g join " +
            "Person p on g.id = p.group.id " +
            "where p.relatedUser.name = :name")
    List<Group> findAllGroupsForUser(@Param("name") String name);

    boolean existsByIdAndCreator_Name(long id, String creatorName);
}
