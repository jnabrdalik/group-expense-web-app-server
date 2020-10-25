package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExpenseInput {

    private int amount;
    private String description;
    private long groupId;
    private long payerId;
    private List<Long> peopleInvolvedIds;

}
