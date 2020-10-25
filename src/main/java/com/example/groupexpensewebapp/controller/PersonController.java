package com.example.groupexpensewebapp.controller;

import com.example.groupexpensewebapp.dto.PersonDetails;
import com.example.groupexpensewebapp.dto.PersonInput;
import com.example.groupexpensewebapp.dto.PersonSummary;
import com.example.groupexpensewebapp.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping("/{id}")
    public PersonDetails getPerson(@PathVariable long id, Principal principal) {
        return personService.getPersonDetails(id, principal.getName());
    }

    @PostMapping
    public PersonSummary addPerson(@RequestBody PersonInput input, Principal principal) {
        return personService.addPerson(input, principal.getName());
    }

    @PutMapping("/{id}")
    public PersonSummary editPerson(@PathVariable long id, @RequestBody PersonInput input, Principal principal) {
        return personService.editPerson(id, input, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable long id, Principal principal) {
        personService.deletePerson(id, principal.getName());
    }
}
