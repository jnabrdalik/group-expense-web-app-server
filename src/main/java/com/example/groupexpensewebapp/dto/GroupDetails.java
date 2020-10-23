package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupDetails {

    private long id;
    private String name;
    private String description;
    private long timeCreated;
    private String creatorUsername;
    private List<ExpenseDetails> expenses;
    private List<PersonSummary> persons;

}
