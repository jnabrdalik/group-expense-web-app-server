package com.example.groupexpensewebapp.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;

    private String password;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private List<Group> createdGroups;

}
