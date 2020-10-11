package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class PersonInput {

    private String name;
    private long groupId;
    private long relatedUserId;

}
