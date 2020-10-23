package com.example.groupexpensewebapp.service;

import com.example.groupexpensewebapp.dto.PersonDetails;
import com.example.groupexpensewebapp.dto.PersonInput;
import com.example.groupexpensewebapp.dto.PersonSummary;
import com.example.groupexpensewebapp.model.Group;
import com.example.groupexpensewebapp.model.Person;
import com.example.groupexpensewebapp.model.UserEntity;
import com.example.groupexpensewebapp.repository.GroupRepository;
import com.example.groupexpensewebapp.repository.PersonRepository;
import com.example.groupexpensewebapp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public PersonService(PersonRepository repository, GroupRepository groupRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.repository = repository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public PersonDetails getPersonDetails(long personId) {
        Person person = repository.findById(personId)
                .orElseThrow(IllegalArgumentException::new);

        return modelMapper.map(person, PersonDetails.class);
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

        long relatedUserId = input.getRelatedUserId();
        if (relatedUserId != 0) {
            UserEntity relatedUser = userRepository.findById(relatedUserId)
                    .orElseThrow(IllegalArgumentException::new);

            person.setRelatedUser(relatedUser);
            person.setName(relatedUser.getName());
        }

        Person addedPerson = repository.save(person);
        return modelMapper.map(addedPerson, PersonSummary.class);
    }

    public PersonSummary editPerson(long personId, PersonInput input) {
        if (input.getName() == null) {
            throw new IllegalArgumentException();
        }

        Person person = repository.findById(personId)
                .orElseThrow(IllegalArgumentException::new);

        person.setName(input.getName());

        repository.save(person);
        return modelMapper.map(person, PersonSummary.class);
    }

    public void deletePerson(long personId) {
        repository.deleteById(personId);
    }


}
