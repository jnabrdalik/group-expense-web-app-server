package com.example.groupexpensewebapp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "expenses")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    private long id;

    private String description;

    private int amount;

    private long timeAdded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Person payer;

    @ManyToMany
    @JoinTable(name = "persons_expenses",
            joinColumns = @JoinColumn(name = "expense_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> peopleInvolved;

}
