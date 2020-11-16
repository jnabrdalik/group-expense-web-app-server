package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ExpenseDetails {

    private long id;
    private String description;
    private int amount;
    private long timestamp;
    private PersonSummary createdBy;
    private PersonSummary payer;
    private Set<PersonSummary> payees;

}
