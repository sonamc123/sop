package com.tashicell.sop.Service;

import com.tashicell.sop.Record.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EndorserService {
    ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getEndorserTaskList(String empId);

    ResponseEntity<SopResponse> approveByEndorser(Integer sopFileId, String updatedBy, String remark);

    ResponseEntity<SopResponse> rejectByEndorser(Integer sopFileId, String updatedBy, String remark);

    ResponseEntity<List<EndorserRemarkInterface>> getEndorserRemark(Integer sopFileId);

    ResponseEntity<SopResponse> approveByAuthorizer(Integer sopFileId, String approvedBy, String remark);

    ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getAuthorizerTaskList();

    byte[] saveSOP(ViewSOPResponsibilityInterface sopTitle, List<HistoryDetailsDTO> changeHistory, Integer sopId, HttpServletRequest request, List<ResponsibilityDTO> responsibilityList);

    void saveEndorsedSOP(String sopName, Integer sopId, byte[] pdfBytes);
}
