package com.example.groupexpensewebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Debt {

    private PersonSummary debtor;
    private PersonSummary creditor;
    private int amount;

}
