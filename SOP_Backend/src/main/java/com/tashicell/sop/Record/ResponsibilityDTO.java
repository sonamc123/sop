package com.tashicell.sop.Record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsibilityDTO {
    private Integer respId;
    private String responsibilityName;
    private Integer roleHolder;
    private String roleHolderName;
    private String remarks;
    private String[] sectionList;
    private String[] sectionNameList;
    private String updatedBy;
    private String content;
    private String oldContent;
    private Integer fileCounter;
    private String[] deptName;
    private String effectiveDate;
    private String departmentName;

    private List<SOPAttachmentDetailsDTO> docLst;
    private List<EndorserActionDTO> actionDTOS;
    private String respStatus;
    private Integer respRelatedID;
    private String relatedStatus;
    private String relatedRemarks;
    private Integer pendingCount;

    private List<RemarksDTO> remarksDTOList;

}
