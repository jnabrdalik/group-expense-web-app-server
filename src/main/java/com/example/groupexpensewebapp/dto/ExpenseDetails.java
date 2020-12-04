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
    private MemberSummary payer;
    private Set<Involvement> involvements;
    private UserSummary creator;
    private long timeCreated;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Involvement {
        private MemberSummary payee;
        private int weight;
    }

}
