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
public class ViewSOPResponsibilityDetailsDTO {

    private String departmentName;
    private String sopTitle;
    private String sopNumber;
    private String authorName;
    private String authorDesignation;
    private String reviewerName;
    private String reviewerDesignation;
    private String authorizerName;
    private String createdOn;
    private String reviewedOn;
    private String authorisedOn;
    private String effectiveDate;
    private String reviewDate;

    private List<ResponsibilityDTO> responsibilityDTOList;


}
