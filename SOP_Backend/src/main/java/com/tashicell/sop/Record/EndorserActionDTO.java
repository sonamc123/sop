package com.tashicell.sop.Record;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndorserActionDTO {

    private String actionTakenBy;
    private String actionTakenOn;
    private String actionStatus;
    private String actionRemarks;

}
