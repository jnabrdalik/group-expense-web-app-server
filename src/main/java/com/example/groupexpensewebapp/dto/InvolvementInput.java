package com.example.groupexpensewebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvolvementInput {
    private long payeeId;
    private int weight;
}
