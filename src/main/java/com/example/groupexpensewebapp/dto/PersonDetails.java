package com.example.groupexpensewebapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class PersonDetails {

    private long id;
    private String name;
    private String relatedUserName;
    private int balance;

}
