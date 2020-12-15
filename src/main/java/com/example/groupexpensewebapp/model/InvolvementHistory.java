package com.example.groupexpensewebapp.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "involvements_history")
@Data
public class InvolvementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne
    @JoinColumn
    private Member payee;

    @ManyToOne
    @JoinColumn
    private ExpenseHistory expense;

    private int weight;

}
