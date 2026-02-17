package com.tashicell.sop.Service;

import com.tashicell.sop.Modal.Responsibilities;
import com.tashicell.sop.Modal.User;
import com.tashicell.sop.Record.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CommonService {
    List<SopTypeRecordInterfaceDTO> getSopType();

    List<DepartmentMasterInterface> getDepartmentMaster();

    List<SectionInterfaceDTO> getSectionMaster();

    List<SectionInterfaceDTO> getSectionByDeptId(Integer deptId);

    List<RoleHolderMasterInterface> getResponsibilityDtls(String empId);

    List<RoleHolderMasterInterface> getRoleMaster();

    ResponseEntity<PendingTaskListCount> getFocalSopCount(String empId);

    User getUserDtls(String empId);

    ResponseEntity<UserDetailsDto> fetchUserDtls(String empId);

    ResponseEntity<Integer> getSopCount(String empId);

    List<StageMasterInterface> fetchStageMaster();
}
