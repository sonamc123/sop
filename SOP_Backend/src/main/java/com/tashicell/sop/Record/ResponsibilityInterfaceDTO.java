package com.tashicell.sop.Record;

import com.tashicell.sop.Repository.Inter_Resp_Mapping_Repo;
import jakarta.persistence.criteria.CriteriaBuilder;

public interface ResponsibilityInterfaceDTO {
    Integer getRespId();
    String getResponsibilityName();
    Integer getRoleHolder();
    String getRoleHolderName();
    String[] getDeptName();
    String[] getSecList();
    String[] getSecNameList();
    String getRemarks();
    String getContent();
    String getOldContent();
    Integer getSopTypeId();
    String getEffectiveDate();

    String getDepartmentName();
    Character getRespDelete();

    Integer getRoleId();

    Integer getRespIdFrom();
    Integer getRespIdTo();
    String getRelatedStatus();
    String getRelatedRemarks();


}
