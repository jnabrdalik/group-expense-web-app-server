package com.example.groupexpensewebapp.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String login;

    private String password;

    private String email;

    @OneToMany(mappedBy = "relatedUser", cascade = CascadeType.ALL)
    private List<Person> relatedPersons;
}
