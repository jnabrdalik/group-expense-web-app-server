package com.example.groupexpensewebapp.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "members")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Member {

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

//    @ManyToMany(mappedBy = "payees")
//    private Set<Expense> expensesInvolvedIn;
//
//    @ManyToMany(mappedBy = "payees")
//    private Set<ExpenseHistory> expensesInvolvedInHistory;

    @OneToMany(mappedBy = "payee")
    private Set<Involvement> involvements;

    @OneToMany(mappedBy = "payee")
    private Set<InvolvementHistory> involvementsHistory;

    @Override
    public String toString() {
        return name;
    }
}
