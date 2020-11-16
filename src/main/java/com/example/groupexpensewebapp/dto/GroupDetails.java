package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupDetails {

    private long id;
    private String name;
    private long timeCreated;
    private boolean registeredOnly;
    private String creatorUserName;
    private List<ExpenseDetails> expenses;
    private List<PersonDetails> persons;

}
