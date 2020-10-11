package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class ExpenseSummary {

    private long id;
    private String description;
    private int amount;
    private long timeAdded;
    private String payerName;

}
