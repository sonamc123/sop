package com.tashicell.sop.Record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestNewEndorsementDTO
{
    private Integer sopID;

    private String sopVersion;

    private Integer sopTypeId;

    private String introduction;

    private Integer departmentId;

    private Integer sectionId;

    List<ResponsibilityDTO> responsibilityDTOList;

    private String createdBy;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    private String updatedBy;
}
