package com.example.groupexpensewebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
public class ExpenseDetails {

    private long id;
    private String description;
    private int amount;
    private long timestamp;
    private MemberDetails payer;
    private Set<InvolvementDetails> involvements;
    private UserSummary creator;
    private long timeCreated;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvolvementDetails {
        private MemberDetails payee;
        private int weight;
    }

}
