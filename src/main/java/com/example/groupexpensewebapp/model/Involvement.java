package com.example.groupexpensewebapp.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "involvements")
@Data
public class Involvement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne
    @JoinColumn
    private Member payee;

    @ManyToOne
    @JoinColumn
    private Expense expense;

    private int weight;

}
