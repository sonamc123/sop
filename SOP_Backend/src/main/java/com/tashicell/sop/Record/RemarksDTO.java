package com.tashicell.sop.Record;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemarksDTO {

    private String remarks;
    private Integer status;
    private String secName;
}
