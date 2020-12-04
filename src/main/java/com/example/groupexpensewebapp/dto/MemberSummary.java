package com.example.groupexpensewebapp.dto;

import lombok.Data;

@Data
public class MemberSummary {

    private long id;
    private String name;
    private String relatedUserName;
}
