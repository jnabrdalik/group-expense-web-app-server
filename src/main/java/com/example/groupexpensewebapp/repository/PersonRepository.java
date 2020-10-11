package com.example.groupexpensewebapp.repository;

import com.example.groupexpensewebapp.model.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {
}
