package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class DebtNotificationInput {

    private String recipientUsername;
    private long groupId;
    private int amount;
}
