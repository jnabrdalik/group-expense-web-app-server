package com.example.groupexpensewebapp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "persons")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    private long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Group group;

    @ManyToOne
    @JoinColumn
    private User relatedUser;

    @OneToMany(mappedBy = "payer")
    private Set<Expense> expensesPaidFor;

    @ManyToMany(mappedBy = "peopleInvolved")
    private Set<Expense> expensesInvolvedIn;
}
