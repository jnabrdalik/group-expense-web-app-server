package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class DebtPayment {

    private long id;
    private int amount;
    private UserSummary payer;
    private UserSummary payee;
    private long timeCreated;

}
