package com.tashicell.sop.Record;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SSOAceessDTO {
    private String empId;
    private String access;
    private String appCode;
}
