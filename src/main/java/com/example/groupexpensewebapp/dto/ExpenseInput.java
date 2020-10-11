package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ExpenseInput {

    private int amount;
    private String description;
    private long groupId;
    private long payerId;
    private Set<Long> peopleInvolvedIds;

}
