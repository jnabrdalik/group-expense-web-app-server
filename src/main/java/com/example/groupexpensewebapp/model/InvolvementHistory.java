package com.example.groupexpensewebapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
