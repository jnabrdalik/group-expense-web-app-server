package com.example.groupexpensewebapp.controller;


import com.example.groupexpensewebapp.dto.PersonDetails;
import com.example.groupexpensewebapp.dto.PersonInput;
import com.example.groupexpensewebapp.dto.PersonSummary;
import com.example.groupexpensewebapp.service.PersonService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/{id}")
    public PersonDetails getPerson(@PathVariable long id) {
        return personService.getPersonDetails(id);
    }

    @PostMapping
    public PersonSummary addPerson(@RequestBody PersonInput input) {
        return personService.addPerson(input);
    }

    @PutMapping("/{id}")
    public PersonSummary editPerson(@PathVariable long id, @RequestBody PersonInput input) {
        return personService.editPerson(id, input);
    }

    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable long id) {
        personService.deletePerson(id);
    }
}
