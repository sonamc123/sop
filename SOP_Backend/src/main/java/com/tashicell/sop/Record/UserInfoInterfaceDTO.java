package com.tashicell.sop.Record;

import javax.security.auth.callback.CallbackHandler;

public interface UserInfoInterfaceDTO {
    Integer getId();
    String getEmpId();
    String getUserName();
    String getPhoneNo();
    String getEmail();
    Integer getRoleId();
    String getRoleName();
    Integer getDeptId();
    String getDeptName();
    Character getGender();
}
