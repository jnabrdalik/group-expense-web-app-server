package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class GroupSummary {

    private long id;
    private String name;
    private String description;
    private long timeCreated;
    private String creatorUsername;
}
