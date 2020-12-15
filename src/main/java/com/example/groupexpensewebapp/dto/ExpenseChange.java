package com.example.groupexpensewebapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExpenseChange {
    private MemberDetails changedBy;
    private long changeTimestamp;
    private List<FieldChange> changes;

    @Data
    @AllArgsConstructor
    public static class FieldChange {
        private String fieldName;
        private String valueBefore;
        private String valueAfter;
    }
}
