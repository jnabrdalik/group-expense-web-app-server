package com.example.groupexpensewebapp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    private long id;

    private String name;

    private String password;

    private String email;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<Group> createdGroups;

    @ManyToMany(mappedBy = "users")
    private Set<Group> groups;

}
