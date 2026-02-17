package com.tashicell.sop.Service;

import com.tashicell.sop.Record.DocumentInterfaceViewDTO;
import com.tashicell.sop.Record.SopResponse;
import com.tashicell.sop.Record.ViewSopDetailsInterfaceDTO;
import com.tashicell.sop.Record.ViewStatusInterface;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FocalPersonService {
    ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getFocalTaskList();

    ResponseEntity<SopResponse> submitForReview(Integer sopFileId, String updatedBy, String remark);

    ResponseEntity<SopResponse> rejectedByFocalPerson(Integer sopFileId, String reviewedBy, String remark);

    ResponseEntity<List<DocumentInterfaceViewDTO>> getSopHistoryByFocal(Integer dept_Id);

    DocumentInterfaceViewDTO downloadEndorsedSop(String docUUID);

    List<ViewStatusInterface> getViewStatus(Integer stageID, Integer deptId);

    List<ViewStatusInterface> getAllStatusDetails();

    List<ViewStatusInterface> getEndorserPendingList(Integer sopId);
}
