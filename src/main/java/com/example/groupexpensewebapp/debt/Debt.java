package com.example.groupexpensewebapp.debt;

import com.example.groupexpensewebapp.dto.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Debt {

    private UserSummary debtor;
    private UserSummary creditor;
    private int amount;

}
