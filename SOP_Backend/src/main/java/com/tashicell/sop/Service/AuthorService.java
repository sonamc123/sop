package com.tashicell.sop.Service;

import com.tashicell.sop.Record.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AuthorService {
    ResponseEntity<SopResponse> createNewSop(RequestNewEndorsementDTO newEndorsementDTO, MultipartFile[] file);



    DocumentInterfaceViewDTO DownloadDocByAuthor(String docUUID);

	ResponseEntity<List<SOPContentDetailsDTO>> viewMyFileContent(String empId);

    ResponseEntity<List<ResponsibilityDTO>> editMyFile(Integer sopID);

    List<ActivityLogInterfaceDTO> getActivityLog(Integer sopId);

    ResponseEntity<ViewSopDetailsInterfaceDTO> getEndorsedSOP(String empId);

    ResponseEntity<List<ResponsibilityDTO>> getResponsibilityListBySopId(Integer sopId);

    ResponseEntity<ResponsibilityDTO> getResponsibilityDtlsByRespId(Integer respId);

    ResponseEntity<SopResponse> addResponsibility(RequestNewEndorsementDTO addResponsibilityDTO, MultipartFile[] addFile);

    ResponseEntity<SopResponse> updateResponsibility(ResponsibilityDTO updateResponsibilityDTO, MultipartFile[] updateFile);
    ResponseEntity<SopResponse> deleteResponsibility(Integer respId, String actionBy);


    ResponseEntity<SopResponse> deleteFileByUUID(String uuid, String actionBy);

    ResponseEntity<List<ResponsibilityDTO>> addendum(Integer sopId, Integer sopTypeId, String introduction, String reason, String updatedBy);

    ResponseEntity<AddendumRemarks> getAddendumDetails(Integer sopId);

    ResponseEntity<SopResponse> submitToFocalPerson(Integer sopFileId, String updatedBy, String remark);

    ResponseEntity<List<RelatedDeptSOPInterface>> getRelatedResponsibility(String empId);

    ResponseEntity<SopResponse> deleteResponsibilityFromEndorsement(Integer respId, String actionBy, String remarks);

    ResponsibilityDTO getRelatedRespDtls(Integer respId);


    void updateRelatedStatus(Integer respId, String userId);

    void updateRelatedRemarks(Integer respId, String userId, String remarks);

    ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getRelatedRemarks(Integer respId);

    ResponseEntity<ResponsibilityDTO> getRelatedActivityDetails(Integer respId, String user);

    ResponseEntity<SopResponse> updateIntro(Integer sopId, String introduction, String updatedBy);
}
