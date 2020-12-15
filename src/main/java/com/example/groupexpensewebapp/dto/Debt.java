package com.example.groupexpensewebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Debt {

    private MemberDetails debtor;
    private MemberDetails creditor;
    private int amount;

}
