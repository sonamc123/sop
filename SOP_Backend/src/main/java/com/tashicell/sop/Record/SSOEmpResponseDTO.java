package com.tashicell.sop.Record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SSOEmpResponseDTO {

    private String message;
    private Integer code;
    private Integer id;
    private String empId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String cidNo;
    private Integer mobileNo;
    private String email;
    private Integer departmentId;
    private String department;
    private Integer sectionId;
    private String section;
    private Integer designationId;
    private String designation;
    private String fullName;
    private String dateOfBirth;
    private Integer extension;
    private String profilePicPath;
    private String gender;
    private String status;

}
