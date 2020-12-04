package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupDetails {

    private long id;
    private String name;
    private long timeCreated;
    private boolean forRegisteredOnly;
    private boolean archived;
    private String creatorName;
    private List<ExpenseDetails> expenses;
    private List<MemberDetails> members;

}
