package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class GroupSummary {

    private long id;
    private String name;
    private long timeCreated;
    private String creatorName;
    private boolean forRegisteredOnly;
    private boolean archived;
}
