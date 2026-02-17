package com.tashicell.sop.Record;

import lombok.Data;

@Data
public class SSO_DTO {
    private String empID;
    private String password;
    private Boolean success;
}
