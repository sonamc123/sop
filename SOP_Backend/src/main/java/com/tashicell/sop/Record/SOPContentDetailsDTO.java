package com.tashicell.sop.Record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SOPContentDetailsDTO {
    private String createdOn;
    private String createdBy;
    private String stageName;
    private Integer sopId;
    private String deptName;
    private String secName;
    private String sopVersion;
    private String addVersion;
    private String introduction;
    private String remarks;
    private Integer isEndorsedId;
}
