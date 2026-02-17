package com.tashicell.sop.Record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class HistoryDetailsDTO {
    private String sopNo;
    private String effectiveDate;
    private String significantChange;
    private String previousSopNo;
    private String isAddendum;

}
