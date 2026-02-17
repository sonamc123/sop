package com.tashicell.sop.Record;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingTaskListCount {
    private String reviewerName;
    private Integer reviewerCount = 0;
    private String endorserCount;
    private Integer authoriserCount;
    private Integer count;
}
