package com.tashicell.sop.Record;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SopResponse {
    private Boolean success;
    private Integer responseCode;
    private String responseText;
    private String sopId;
    private String sopName;

}
