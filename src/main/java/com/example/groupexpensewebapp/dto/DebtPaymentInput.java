package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class DebtPaymentInput {

    private int amount;
    private long groupId;
    private long payerId;
    private long payeeId;

}
