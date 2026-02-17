package com.tashicell.sop.Record;

import com.tashicell.sop.Modal.RoleMaster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {
    private String userName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String password;
    private Integer department;
    private String departmentName;
    private Integer section;
    private String sectionName;
    private List<PrivilegeDTO> privileges;
    private List<RoleMaster> roleMaster;
    private Integer roleId;
    private Integer designation;
    private String designationName;
    private String role;
    private String createdBy;
    private String updatedBy;
    private Character userStatus;
    private Character gender;
}
