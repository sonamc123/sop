package com.tashicell.sop.Service;

import com.tashicell.sop.Record.ResponsibilityDTO;
import com.tashicell.sop.Record.SopResponse;
import com.tashicell.sop.Record.ViewSopDetailsInterfaceDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReviewerService {
    ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getReviewerTaskList(String empId);

    ResponseEntity<SopResponse> approveByReviewer(Integer sopFileId, String reviewedBy, String remark);

    ResponseEntity<SopResponse> rejectedByReview(Integer sopFileId, String reviewedBy, String remark);

    ResponseEntity<List<ResponsibilityDTO>> getResponsibilityListByReviewer(Integer sopFileId);

    ResponseEntity<SopResponse> updateContentByReviewer(Integer respId, String respContent, String updatedBy);
}
