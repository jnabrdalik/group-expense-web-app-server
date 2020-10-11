package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.PersonDetails;
import com.example.groupexpensewebapp.dto.PersonInput;
import com.example.groupexpensewebapp.dto.PersonSummary;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.PersonRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final GroupRepository groupRepository;

    public PersonService(PersonRepository repository, GroupRepository groupRepository) {
        this.repository = repository;
        this.groupRepository = groupRepository;
    }

    public PersonDetails getPersonDetails(long personId) {
        Person person = repository.findById(personId)
                .orElseThrow(IllegalArgumentException::new);

        return mapToPersonDetails(person);
    }

    public PersonSummary addPerson(PersonInput input) {
        if (input.getName() == null) {
            throw new IllegalArgumentException();
        }

        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(IllegalArgumentException::new);

        Person person = new Person();
        person.setName(input.getName());
        person.setGroup(group);

        Person addedPerson = repository.save(person);
        return mapToPersonSummary(addedPerson);
    }

    public PersonSummary editPerson(long personId, PersonInput input) {
        if (input.getName() == null) {
            throw new IllegalArgumentException();
        }

        Person person = repository.findById(personId)
                .orElseThrow(IllegalArgumentException::new);

        person.setName(input.getName());

        repository.save(person);
        return mapToPersonSummary(person);
    }

    public void deletePerson(long personId) {
        repository.deleteById(personId);
    }

    public PersonSummary mapToPersonSummary(Person person) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(person, PersonSummary.class);
    }

    public PersonDetails mapToPersonDetails(Person person) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(person, PersonDetails.class);
    }
}
