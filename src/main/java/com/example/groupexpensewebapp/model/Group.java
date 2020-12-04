package com.example.groupexpensewebapp.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "groups")
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String name;

    private long timeCreated;

    private boolean forRegisteredOnly;

    private boolean archived;

    @ManyToOne
    private UserEntity creator;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Member> members;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Expense> expenses;

}
