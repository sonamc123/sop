package com.tashicell.sop.Record;

public interface ViewSopDetailsInterfaceDTO {
    String getCreatedOn();
    String getCreatedBy();
    String getStageName();
    Integer getSopId();
    String getSopVersion();
    String getAddVersion();
    String getDeptName();
    String getSecName();
    String getIntroduction();
    String getRemarks();
    Integer getIsEndorsed();

    String getUpdatedOn();
}
