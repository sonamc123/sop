package com.tashicell.sop.Service;

import com.tashicell.sop.Record.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ViewerService {

    ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getViewerTaskList(String userId);

    ResponseEntity<List<HistoryDetailsDTO>> getChangeHistory(Integer sopId, String userId);

    ResponseEntity<ViewSOPResponsibilityInterface> getSopTitleDetails(Integer sopId);

    ResponseEntity<ViewSopDetailsInterfaceDTO> getSopDescription(Integer sopId);

    ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getOtherDepartmentSOP(Integer deptId);

    ResponseEntity<List<ResponsibilityDTO>> getResponsibilityListBySopId(Integer sopId);

    ResponseEntity<ResponsibilityDTO> getResponsibilityDtlsByViewer(Integer respId);

    List<ActivityLogInterfaceDTO> getViewerActivityLog(Integer sopId, String sopVersion);

	byte[] generatePdf(ViewSOPResponsibilityInterface viewSOPTitle, List<HistoryDetailsDTO> changeHistory, Integer sopId, HttpServletRequest request, List<ResponsibilityDTO> responsibilityListBySopId);

    ResponsibilityDTO getHyperlinkRespContent(Integer respId);

    ResponseEntity<List<RelatedDeptSOPInterface>> getRelatedActivities(String empId);

    ResponseEntity<List<RelatedDeptSOPInterface>> getOtherRelatedActivities(Integer sopId);
}
