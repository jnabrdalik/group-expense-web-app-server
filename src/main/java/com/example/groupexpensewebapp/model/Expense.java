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
    private Member payer;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Involvement> involvements;

    private long timeCreated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Group group;

    @OneToMany(mappedBy = "originalExpense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseHistory> previousVersions;

}
