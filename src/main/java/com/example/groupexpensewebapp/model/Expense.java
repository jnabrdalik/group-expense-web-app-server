package com.example.groupexpensewebapp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;
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

    private long timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Person payer;

    @ManyToMany
    @JoinTable(name = "expenses_persons",
            joinColumns = @JoinColumn(name = "expense_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> payees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Group group;

    private long createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Person createdBy;

    private long lastEditTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Person lastEditedBy;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    private List<ExpenseHistory> history;

}
