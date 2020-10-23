package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class PersonDetails {

    private long id;
    private String name;
    private long relatedUserId;
    private List<ExpenseSummary> expensesPaidFor;

}
