package com.example.groupexpensewebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Debt {

    private MemberSummary debtor;
    private MemberSummary creditor;
    private int amount;

}
