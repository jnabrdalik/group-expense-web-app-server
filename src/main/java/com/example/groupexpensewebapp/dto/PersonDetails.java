package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class PersonDetails {

    private long id;
    private String name;
    private List<ExpenseSummary> expensesPaidFor;

}
