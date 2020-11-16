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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository repository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final MailService mailService;

    public PersonDetails getPersonDetails(long personId, String username) {
        Person person = repository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long groupId = person.getGroup().getId();
        if (!repository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return modelMapper.map(person, PersonDetails.class);
    }

    public PersonSummary addPerson(PersonInput input, String username) {
        if (!repository.existsByRelatedUserName_AndGroup_Id(username, input.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        
        Group group = groupRepository.findById(input.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        Person person = new Person();
        person.setName(input.getName());
        person.setGroup(group);

        long relatedUserId = input.getRelatedUserId();
        if (relatedUserId != 0) {
            UserEntity relatedUser = userRepository.findById(relatedUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            if (repository.existsByRelatedUserName_AndGroup_Id(relatedUser.getName(), group.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in this group!");
            }

            person.setRelatedUser(relatedUser);
            person.setName(relatedUser.getName());
        }

        Person addedPerson = repository.save(person);
        return modelMapper.map(addedPerson, PersonSummary.class);
    }

    public PersonSummary editPerson(long personId, PersonInput input, String username) {
        Person person = repository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long groupId = person.getGroup().getId();
        if (!repository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (input.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        person.setName(input.getName());

        repository.save(person);
        return modelMapper.map(person, PersonSummary.class);
    }

    public void deletePerson(long personId, String username) {
        Person person = repository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long groupId = person.getGroup().getId();
        if (!repository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        UserEntity relatedUser = person.getRelatedUser();
        if (relatedUser != null && groupRepository.existsByIdAndCreator_Name(groupId, relatedUser.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete group creator!");
        }

        repository.deleteById(personId);
    }

    public void sendInvite(long personId, String email, String username) {
        if (!mailService.isEmailValid(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Person person = repository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long groupId = person.getGroup().getId();
//        if (!repository.existsByRelatedUserName_AndGroup_Id(username, groupId)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
//        }

        UserEntity user = new UserEntity();
        userRepository.save(user);

        person.setRelatedUser(user);
        repository.save(person);

        mailService.sendMessage(email, "Zaproszenie do aplikacji", "Cześć, " + person.getName() +
                "! Zapraszam cię do grupy " + person.getGroup().getName() + ". Aby się zarejestrować, kliknij " +
                "w poniższy link:\n" + "http://localhost:4200/sign-up/invite/" + personId);
    }
}
