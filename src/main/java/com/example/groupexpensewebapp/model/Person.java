package com.example.groupexpensewebapp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;
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
    private UserEntity relatedUser;

    @OneToMany(mappedBy = "payer")
    private Set<Expense> expensesPaidFor;

    @ManyToMany(mappedBy = "payees")
    private Set<Expense> expensesInvolvedIn;

    @ManyToMany(mappedBy = "payees")
    private Set<ExpenseHistory> expensesInvolvedInHistory;

    @OneToMany(mappedBy = "createdBy")
    private List<Expense> expensesCreated;

    @Override
    public String toString() {
        return name;
    }
}
