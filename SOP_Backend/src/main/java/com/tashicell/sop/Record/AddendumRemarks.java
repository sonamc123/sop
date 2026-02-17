package com.tashicell.sop.Record;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddendumRemarks {

    private Integer sopId;
    private String introduction;
    private String deptName;
    private String secName;
    private String sopVersion;
    private String addendReason;
    private Integer updateType;

}
