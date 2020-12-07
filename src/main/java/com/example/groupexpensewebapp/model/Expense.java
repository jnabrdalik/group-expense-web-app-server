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
    private User payer;

    @ManyToMany
    @JoinTable(name = "users_expenses",
            joinColumns = @JoinColumn(name = "expense_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> payees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Group group;

    private long timeCreated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Expense originalExpense;

    @OneToMany(mappedBy = "originalExpense", cascade = CascadeType.ALL)
    private List<Expense> previousVersions;

}
