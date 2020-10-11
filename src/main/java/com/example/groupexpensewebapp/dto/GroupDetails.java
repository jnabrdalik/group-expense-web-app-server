package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupDetails {

    private long id;
    private String name;
    private String description;
    private long timeCreated;
    private List<ExpenseSummary> expenses;
    private List<PersonSummary> persons;

}