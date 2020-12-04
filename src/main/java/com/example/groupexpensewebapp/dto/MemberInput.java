package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class MemberInput {

    private String name;
    private long groupId;
    private String relatedUserName;

}
