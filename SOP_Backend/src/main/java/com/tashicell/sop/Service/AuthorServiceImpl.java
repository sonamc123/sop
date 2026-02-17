package com.tashicell.sop.Service;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.tashicell.sop.Enum.StageMasterEnum;
import com.tashicell.sop.Exception.RecordNotFoundException;
import com.tashicell.sop.Modal.*;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.*;
import com.tashicell.sop.Utility.MailSender;
import com.tashicell.sop.Utility.SMSSenderHttp;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AuthorServiceImpl implements AuthorService {

    private final SopTypeMasterRepo sopTypeMasterRepo;
    private final SopFileRepo sopFileRepo;
    private final SopStageRepo sopStageRepo;
    private final SopWorkFLowRepo workFLowRepo;

    private final DocuRepo docuRepo;

    private final UserRepo userRepo;

    private final LogRepo logRepo;

    private final CommonServiceImpl commonService;

    private final DepartmentRepo departmentRepo;

    private final ResponsibilityRepo responsibilityRepo;

    private final Resp_Dept_Mapping_Repo resp_sec_mapping_repo;

    private final SectionRepo sectionRepo;

    private final ChangeHistoryRepo changeHistoryRepo;

    private final FocalPersonServiceImpl focalPersonService;

    private final Inter_Resp_Mapping_Repo inter_resp_mapping_repo;

    @Override
    public ResponseEntity<SopResponse> createNewSop(RequestNewEndorsementDTO newEndorsementDTO, MultipartFile[] file) {
        SopResponse sopResponse = new SopResponse();
        try {
            SopTypeMaster sopTypeMaster = sopTypeMasterRepo.findById(1)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Type ID not found with ID: " + 1));

            User user = commonService.getUserDtls(newEndorsementDTO.getCreatedBy());

            DepartmentMaster departmentMasterId = commonService.getDeptId(user.getDepartmentMaster().getId());
            SectionMaster sectionMasterId = commonService.getSecId(user.getSectionMaster().getId());

            //BigInteger checkDuplicateSOP = sopFileRepo.checkDuplicateSOP(sectionMasterId.getId());
            Integer sopCount = sopFileRepo.getSopCount(sectionMasterId.getId());

            StageMaster stageMaster = sopStageRepo.findById(StageMasterEnum.initiated.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.initiated.getStageId()));

            if (sopCount == 0){
                //Create SOP details table

                var sopFile = SopFileDetails.builder()
                        .sopTypeMaster(sopTypeMaster)
                        .createdBy(user)
                        .createdOn(LocalDateTime.now())
                        .sopVersion("V1.0")
                        .deptID(departmentMasterId)
                        .secID(sectionMasterId)
                        .introduction(newEndorsementDTO.getIntroduction())
                        .build();
                var saveSopFile = sopFileRepo.save(sopFile);

                if (!CollectionUtils.isEmpty(newEndorsementDTO.getResponsibilityDTOList())) {
                    for (ResponsibilityDTO responsibilityDTO : newEndorsementDTO.getResponsibilityDTOList()) {

                        if (responsibilityDTO.getRespId() != null){
                            Responsibilities responsibilityName = responsibilityRepo.findById(responsibilityDTO.getRespId())
                                    .orElseThrow(() -> new IllegalArgumentException("Activity Details not found with ID: " + responsibilityDTO.getRespId()));

                            responsibilityDTO.setResponsibilityName(responsibilityName.getResponsibilityName());
                        }

                        Responsibilities responsibilities = new Responsibilities();
                        responsibilities.setResponsibilityName(responsibilityDTO.getResponsibilityName());
                        responsibilities.setSopId(saveSopFile);
                        responsibilities.setRemarks(responsibilityDTO.getRemarks());
                        responsibilities.setContent(responsibilityDTO.getContent());
                        responsibilities.setTypeId(sopTypeMaster);
                        responsibilities.setSopVersion(saveSopFile.getSopVersion());
                        responsibilities.setUpdatedOn(LocalDateTime.now());
                        responsibilities.setIsAddendum('N');
                        responsibilities.setIsEndorsed('N');

                        Responsibilities respStatus = responsibilityRepo.save(responsibilities);

                        List<Responsibility_Section_mapping> responsibilityDepartmentMappingList = new ArrayList<>();
                        List<InterRelatedResponsibility_Mapping> lst = new ArrayList<>();

                        if(responsibilityDTO.getSectionList().length != 0){

                            for (int i = 0; i < responsibilityDTO.getSectionList().length; i++) {

                                Responsibility_Section_mapping responsibilityDepartmentMapping = new Responsibility_Section_mapping();

                                Integer secID = Integer.valueOf(responsibilityDTO.getSectionList()[i]);

                                Integer dept = responsibilityRepo.getDeptByID(secID);

                                DepartmentMaster departmentMaster = departmentRepo.findById(dept)
                                        .orElseThrow(() -> new IllegalArgumentException("Department Details not found with ID: " + dept));

                                SectionMaster sectionMaster = sectionRepo.findById(secID)
                                        .orElseThrow(() -> new IllegalArgumentException("Department Details not found with ID: " + secID));

                                responsibilityDepartmentMapping.setResponsibilities(getRespById(respStatus.getResId()));
                                responsibilityDepartmentMapping.setDepartmentMasterList(departmentMaster);
                                responsibilityDepartmentMapping.setSectionMaster(sectionMaster);
                                responsibilityDepartmentMapping.setStatus('P');

                                responsibilityDepartmentMappingList.add(responsibilityDepartmentMapping);
                            }

                            resp_sec_mapping_repo.saveAll(responsibilityDepartmentMappingList);

                            for (int i = 0; i < responsibilityDTO.getSectionList().length; i++) {

                                //To send notification to concern Section Head when there is related sop/

                                InterRelatedResponsibility_Mapping interRelatedResponsibilityMapping = new InterRelatedResponsibility_Mapping();

                                Integer secID = Integer.valueOf(responsibilityDTO.getSectionList()[i]);

                                SectionMaster sectionMaster = sectionRepo.findById(secID)
                                        .orElseThrow(() -> new IllegalArgumentException("Department Details not found with ID: " + secID));

                                UserInfoInterfaceDTO userInfoInterfaceDTO = userRepo.getRelatedSectionHead(secID, respStatus.getResId());

                                if(responsibilityDTO.getRespId() != null){
                                    updateInterDeptActivity(responsibilityDTO.getRespId(), user.getSectionMaster().getId(),respStatus.getResId());
                                }

                                interRelatedResponsibilityMapping.setRespFrom(respStatus);
                                interRelatedResponsibilityMapping.setSecTo(sectionMaster);
                                lst.add(interRelatedResponsibilityMapping);

                                SendNotificationToRelatedRespSecHead(userInfoInterfaceDTO, user, responsibilityDTO);
                            }
                            inter_resp_mapping_repo.saveAll(lst);

                        }else {
                            if(responsibilityDTO.getRespId() != null){
                                updateInterDeptActivity(responsibilityDTO.getRespId(), user.getSectionMaster().getId(),respStatus.getResId());
                            }
                        }
                        if (file.length > 0) {

                            var updateChangeHistoryEntity = HistoryDetailsEntity.builder()
                                    .sopNo(saveSopFile.getId())
                                    .previousSopNo("NA")
                                    .currentSopNo(saveSopFile.getSopVersion())
                                    .sopType(saveSopFile.getSopTypeMaster())
                                    .isAddendum('N')
                                    .build();

                            changeHistoryRepo.save(updateChangeHistoryEntity);

                            var logEntity = SopLogDetails.builder()
                                    .actionTime(LocalDateTime.now())
                                    .actionTakenBy(user)
                                    .sopFileId(saveSopFile)
                                    .sopVersion(saveSopFile.getSopVersion())
                                    .action("Created new Activity with Name "+responsibilityDTO.getResponsibilityName())
                                    .build();
                            logRepo.save(logEntity);

                            @SuppressWarnings("unused")
							Boolean uploadDocStatus = saveMultipleDocumentDetails(file, user, saveSopFile, responsibilities, responsibilityDTO.getFileCounter());
                        }
                    }
                }
                //Update workflow table
                var sopWorkFlow = WorkFlowDetails.builder()
                        .actionTakenBy(user)
                        .actionTakenOn(LocalDateTime.now())
                        .fileId(saveSopFile)
                        .stageMaster(stageMaster)
                        .sopVersion(saveSopFile.getSopVersion())
                        .build();
                var saveSopWorkFlow = workFLowRepo.save(sopWorkFlow);

                if (saveSopWorkFlow != null) {
                    sopResponse.setResponseText("Proposed SOP submitted Successfully");
                    sopResponse.setSuccess(true);
                    sopResponse.setResponseCode(201);
                } else {
                    sopResponse.setResponseText("Failed to Submit New SOP Endorsement");
                    sopResponse.setSuccess(false);
                    sopResponse.setResponseCode(500);

                    throw new RecordNotFoundException("Failed to Submit New SOP Endorsement");
                }
            }else {
                sopResponse.setResponseText("You are allow to create one SOP per section, which you have already created for your section. Please check MyFile Feature to update your draft SOP.");
                sopResponse.setSuccess(false);
                sopResponse.setResponseCode(1);
                System.out.println(sopResponse);

                throw new RecordNotFoundException("Failed to Submit New SOP due to your previous SOP in process of Approval:" +user.getSectionMaster().getSectionName() );
            }
            return ResponseEntity.created(null).body(sopResponse);

        } catch (Exception e) {
            log.error(e);
            sopResponse.setResponseText(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
    }

    private void updateInterDeptActivity(Integer respFrom, Integer secId, Integer respTo) {
        Integer sectionId = inter_resp_mapping_repo.getRelatedSecIdTo(respFrom, secId);
        if (sectionId.intValue() != 0){
            inter_resp_mapping_repo.updateInter_Resp_Mapping(respTo, respFrom, secId);
        }
    }

    @Override
    public ResponseEntity<List<SOPContentDetailsDTO>> viewMyFileContent(String empId) {

        try {
            List<SOPContentDetailsDTO> sopContentDetailsDTOs = new ArrayList<>();
            User userDetails = commonService.getUserDtls(empId);

            List<ViewSopDetailsInterfaceDTO> viewSopDetailsInterfaceDTOList = sopFileRepo.findSopDtlsBySectionId(userDetails.getSectionMaster().getId());

            if (!CollectionUtils.isEmpty(viewSopDetailsInterfaceDTOList)) {
                for (ViewSopDetailsInterfaceDTO content : viewSopDetailsInterfaceDTOList) {
                    if(content.getIsEndorsed() == null || content.getIsEndorsed() != 8){
                        SOPContentDetailsDTO sopDetails = new SOPContentDetailsDTO(content.getCreatedOn(), content.getCreatedBy(), content.getStageName(),
                                content.getSopId(), content.getDeptName(), content.getSecName(), content.getSopVersion(), content.getAddVersion(), content.getIntroduction(), content.getRemarks(), content.getIsEndorsed());

                        sopContentDetailsDTOs.add(sopDetails);
                    }
                }
            }
            return ResponseEntity.ok().body(sopContentDetailsDTOs);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<List<ResponsibilityDTO>> editMyFile(Integer sopID) {
        List<ResponsibilityDTO> responsibilitiesLst = new ArrayList<>();
        try {
            List<ResponsibilityInterfaceDTO> responsibilityInterfaceDTOList = responsibilityRepo.viewResponsibilitiesLstBySop(sopID);

            if (!CollectionUtils.isEmpty(responsibilityInterfaceDTOList)) {

                for (ResponsibilityInterfaceDTO responsibilityInterfaceDTO : responsibilityInterfaceDTOList) {

                    List<DocumentInterfaceViewDTO> docList = docuRepo.getDocList(responsibilityInterfaceDTO.getRespId());

                    List<SOPAttachmentDetailsDTO> attachmentList = new ArrayList<>();

                    if (!CollectionUtils.isEmpty(docList)) {
                        for (DocumentInterfaceViewDTO docDTO : docList) {
                            SOPAttachmentDetailsDTO attachment = new SOPAttachmentDetailsDTO(docDTO.getDocUUID(), docDTO.getDocName());
                            attachmentList.add(attachment);
                        }
                    } else {
                        attachmentList.add(null);
                    }
                    String status;
                    if (responsibilityInterfaceDTO.getRespDelete().charValue() == 'Y'){
                        status = "In-Active";
                    }else {
                        status = "Active";
                    }
                    ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(),
                            responsibilityInterfaceDTO.getResponsibilityName(), null, responsibilityInterfaceDTO.getRoleHolderName(), responsibilityInterfaceDTO.getRemarks(),
                            responsibilityInterfaceDTO.getSecList(), null, null, responsibilityInterfaceDTO.getContent(), responsibilityInterfaceDTO.getOldContent(),
                            null, responsibilityInterfaceDTO.getDeptName(),responsibilityInterfaceDTO.getEffectiveDate(), responsibilityInterfaceDTO.getDepartmentName(),
                            attachmentList, null, status, null, null, null, null, null);

                    responsibilitiesLst.add(responsibilityDTO);
                }
            }
            List<EndorserActionTypeInterface> endorserActionList = sopFileRepo.getEndorserActionList(sopID);

            List<EndorserActionDTO> endorserActionDTOList = new ArrayList<>();

            if (endorserActionList != null) {
                for (EndorserActionTypeInterface endorserAction : endorserActionList) {
                    EndorserActionDTO endorserActionDTO = new EndorserActionDTO(endorserAction.getActionTakenBy(), endorserAction.getActionTakenOn(), endorserAction.getActionStatus(), endorserAction.getActionRemarks());
                    endorserActionDTOList.add(endorserActionDTO);
                }
                responsibilitiesLst.get(0).setActionDTOS(endorserActionDTOList);
            }
            return ResponseEntity.ok().body(responsibilitiesLst);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<List<ResponsibilityDTO>> addendum(Integer sopId, Integer sopTypeId, String introduction, String reason, String updatedBy) {

        List<ResponsibilityDTO> responsibilityDTOList = new ArrayList<>();
        try {
            if (sopId != null) {
                SopFileDetails updateSopEntity = sopFileRepo.findById(sopId)
                        .orElseThrow(() -> new IllegalArgumentException("SOP Type ID not found with ID: " + sopId));
                SopTypeMaster updateSopTypeId = sopTypeMasterRepo.findById(sopTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("SOP Type ID not found with ID: " + sopTypeId));

                String sopVersion = changeHistoryRepo.getSopVersion(sopId);
                if (updateSopEntity != null) {

                    String currentVersion[] = sopVersion.split("\\.");
                    String part1 = currentVersion[0];
                    Integer part2 = Integer.valueOf(currentVersion[1]);
                    String updatedVersion = part1+"."+(part2+1);
                    updateSopEntity.setSopVersion(updatedVersion);

                    if (updatedBy != null) {
                        updateSopEntity.setUpdatedBy(commonService.getUserDtls(updatedBy));
                    }

                    updateSopEntity.setSopTypeMaster(updateSopTypeId);

                    if (introduction != null) {
                        updateSopEntity.setIntroduction(introduction);
                    }
                    updateSopEntity.setAddendumReason(reason);

                    updateSopEntity.setUpdatedOn(LocalDateTime.now());

                    sopFileRepo.save(updateSopEntity);

                    sopFileRepo.deleteEndorserTasklistOnAddendum(sopId);

                    responsibilityDTOList = getResponsibilityList(sopId);

                    var updateChangeHistoryEntity = HistoryDetailsEntity.builder()
                            .sopNo(updateSopEntity.getId())
                            .previousSopNo(sopVersion)
                            .currentSopNo(updatedVersion)
                            .significantChanges(reason)
                            .sopType(updateSopTypeId)
                            .isEndorsed('N')
                            .isAddendum('Y')
                            .build();

                    changeHistoryRepo.save(updateChangeHistoryEntity);

                    var logEntity = SopLogDetails.builder()
                            .actionTime(LocalDateTime.now())
                            .actionTakenBy(commonService.getUserDtls(updatedBy))
                            .sopFileId(updateSopEntity)
                            .remarks(introduction)
                            .sopVersion(updateSopEntity.getSopVersion())
                            .action("Addendum on SOP")
                            .build();
                    logRepo.save(logEntity);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok().body(responsibilityDTOList);
    }

    @Override
    public ResponseEntity<AddendumRemarks> getAddendumDetails(Integer sopId) {
        AddendumRemarks addendumRemarks = new AddendumRemarks();
        if (sopId != null){
            SopFileDetails sopDlts = sopFileRepo.findById(sopId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopId));
            addendumRemarks = new AddendumRemarks(sopId, sopDlts.getIntroduction(), sopDlts.getDeptID().getDepartmentName(), sopDlts.getSecID().getSectionName(), sopDlts.getSopVersion(), sopDlts.getAddendumReason(), sopDlts.getSopTypeMaster().getId());
        }
        return ResponseEntity.ok().body(addendumRemarks);
    }

    @Override
    public ResponseEntity<SopResponse> submitToFocalPerson(Integer sopFileId, String updatedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        try {

            User user = commonService.getUserDtls(updatedBy);

            if(user.getRole().getId() == 3) {
                ResponseEntity<SopResponse> response = focalPersonService.submitForReview(sopFileId, updatedBy, remark);
                sopResponse = response.getBody();

            }else{
                StageMaster focalStageMaster = sopStageRepo.findById(StageMasterEnum.focalPersonReview.getStageId())
                        .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.focalPersonReview.getStageId()));

                SopFileDetails sopFileDetails = sopFileRepo.findById(sopFileId)
                        .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));

                WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopFileId)
                        .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopFileId));

                if (workFlowDetails != null) {
                    workFlowDetails.setStageMaster(focalStageMaster);
                    workFlowDetails.setActionTakenBy(user);
                    workFlowDetails.setRemarks(remark);
                    //workFlowDetails.setSopVersion(sopFileDetails.getSopVersion());
                    workFlowDetails.setActionTakenOn(LocalDateTime.now());

                    workFLowRepo.save(workFlowDetails);

                    var logEntity = SopLogDetails.builder()
                            .actionTime(LocalDateTime.now())
                            .actionTakenBy(user)
                            .sopFileId(sopFileDetails)
                            .sopVersion(sopFileDetails.getSopVersion())
                            .remarks(remark)
                            .action("Submitted to Focal Person")
                            .build();
                    logRepo.save(logEntity);

                    List<User> focalDetailList = userRepo.getFocalPersonDtls(6);

                    for (User focalDetails : focalDetailList){
                        String gender = "";
                        if (focalDetails.getGender().charValue() == 'M'){
                            gender = "Mr";
                        }else {
                            gender = "Ms";
                        }

                        String fullName = focalDetails.getFirstName() + focalDetails.getLastName();
                        String mailContent =
                                "<html> <body>"+"Dear Sir/Madam,<br>"
                                        + "The "+user.getSectionMaster().getSectionName()+ " section within the "+user.getDepartmentMaster().getDepartmentShortCode()+ " department has submitted a proposed SOP for verification, kindly proceed with the appropriate actions.<br>"
                                        + "<p>Thank You</p>"
                                        +"</body></html>";

                        String smsContent =
                                "Dear Sir/Madam,\n"
                                        + "The "+user.getSectionMaster().getSectionName()+ " section within the "+user.getDepartmentMaster().getDepartmentShortCode()+ " department has submitted a proposed SOP for verification, kindly proceed with the appropriate actions.\n"
                                        + "Thank You";
                        try{

                            MailSender.sendMail(focalDetails.getEmailId(), null, null, mailContent, "SOP Focal Person Verification");
                            SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
                            smsSenderHttp.sendSMS(focalDetails.getMobileNo(), smsContent);

                        } catch (Exception e) {
                            sopResponse.setResponseText(e.getMessage());
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
                            throw  new RuntimeException(e.getMessage());
                        }
                    }

                    sopResponse.setResponseText("SOP Successfully Submitted to Focal Person");
                    sopResponse.setSuccess(true);
                    sopResponse.setResponseCode(1);
                } else {
                    sopResponse.setResponseText("Failed to Submit SOP");
                    sopResponse.setSuccess(false);
                    sopResponse.setResponseCode(0);
                }
            }

        } catch (Exception e) {
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    @Override
    public ResponseEntity<List<RelatedDeptSOPInterface>> getRelatedResponsibility(String empId) {
        try {
            User userDetails = commonService.getUserDtls(empId);
            List<RelatedDeptSOPInterface> viewRelatedResp = sopFileRepo.getRelatedResponsibility(userDetails.getSectionMaster().getId());
            
            return ResponseEntity.ok().body(viewRelatedResp);
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<SopResponse> deleteResponsibilityFromEndorsement(Integer respId, String actionBy, String remarks) {
        SopResponse response = new SopResponse();
        try {
            Responsibilities deleteObsoleteResponsibility = responsibilityRepo.findById(respId)
                    .orElseThrow(() -> new IllegalArgumentException("responsibility Details not found with ID: " + respId));
            SopFileDetails sopFileDetails = sopFileRepo.findById(deleteObsoleteResponsibility.getSopId().getId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + deleteObsoleteResponsibility.getSopId().getId()));

            SopTypeMaster updateSopTypeId = sopTypeMasterRepo.findById(2)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Type ID not found with ID: " + 2));

            if (deleteObsoleteResponsibility.getResId() != null){
                deleteObsoleteResponsibility.setWantToDelete('Y');
                deleteObsoleteResponsibility.setRemarks(remarks);
                deleteObsoleteResponsibility.setUpdatedBy(commonService.getUserDtls(actionBy));
                deleteObsoleteResponsibility.setUpdatedOn(LocalDateTime.now());

                if (sopFileDetails.getSopTypeMaster().getId() == 2){
                    deleteObsoleteResponsibility.setSopVersion(sopFileDetails.getSopVersion());
                    deleteObsoleteResponsibility.setTypeId(updateSopTypeId);
                    deleteObsoleteResponsibility.setIsAddendum('Y');
                }
            }
            responsibilityRepo.save(deleteObsoleteResponsibility);

            var logEntiy = SopLogDetails.builder()
                    .actionTime(LocalDateTime.now())
                    .actionTakenBy(commonService.getUserDtls(actionBy))
                    .sopFileId(sopFileDetails)
                    .sopVersion(sopFileDetails.getSopVersion())
                    .action("Submitted Proposal to delete Activity Name "+deleteObsoleteResponsibility.getResponsibilityName())
                    .build();
            logRepo.save(logEntiy);

            response.setResponseText("Submitted Successfully");
            response.setSuccess(true);
            response.setResponseCode(1);

        } catch (Exception e) {
            response.setResponseText(e.getMessage());
            response.setResponseText("Activity failed to Deleted");
            response.setSuccess(false);
            response.setResponseCode(0);
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    private List<ResponsibilityDTO> getResponsibilityList(Integer sopID) {

        List<ResponsibilityDTO> responsibilityDTOList = new ArrayList<>();

            List<ResponsibilityInterfaceDTO> responsibilityInterfaceList = responsibilityRepo.viewResponsibilitiesLstBySop(sopID);
            if (responsibilityInterfaceList != null) {
                for (ResponsibilityInterfaceDTO responsibilityInterfaceDTO : responsibilityInterfaceList) {

                    String status;
                    if (responsibilityInterfaceDTO.getRespDelete().charValue() == 'Y'){
                        status = "In-Active";
                    }else {
                        status = "Active";
                    }

                    List<String> statusCheck = resp_sec_mapping_repo.checkRelatedStatus(responsibilityInterfaceDTO.getRespId());

                    String relatedStatus = "";
                    boolean hasPending = false;
                    boolean hasDisagreed = false;
                    boolean hasAgreed = false;

                    if (statusCheck != null) {
                        for (int i = 0; i < statusCheck.size(); i++) {
                            if (statusCheck.get(i).equalsIgnoreCase("P")) {
                                relatedStatus = "Pending";
                                hasPending = true;
                            } else if (statusCheck.get(i).equalsIgnoreCase("D")) {
                                relatedStatus = "Disagreed";
                                hasDisagreed = true;
                            } else if (statusCheck.get(i).equalsIgnoreCase("A")) {
                                relatedStatus = "Agreed";
                                hasAgreed = true;
                            }
                        }

                        // Set relatedStatus based on the conditions
                        if (hasDisagreed && hasPending) {
                            relatedStatus = "Disagreed";
                        } else if (hasPending ) {
                            relatedStatus = "Pending";
                        }else if (hasDisagreed ) {
                            relatedStatus = "Disagreed";
                        } else if (hasAgreed ){
                            relatedStatus = "Agreed";
                        }
                    }

                    ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(), responsibilityInterfaceDTO.getResponsibilityName(), null,
                            null, null, responsibilityInterfaceDTO.getSecList(), null,
                            null, null, null, null, responsibilityInterfaceDTO.getDeptName(), responsibilityInterfaceDTO.getEffectiveDate(), responsibilityInterfaceDTO.getDepartmentName(),
                            null, null, status,null, relatedStatus, responsibilityInterfaceDTO.getRelatedRemarks(), null, null);

                    responsibilityDTOList.add(responsibilityDTO);
                }
            }
            return responsibilityDTOList;
    }

    @Override
    public DocumentInterfaceViewDTO DownloadDocByAuthor(String docUUID) {
        return this.docuRepo.DownloadDocByAuthor(docUUID);
    }

    @Override
    public List<ActivityLogInterfaceDTO> getActivityLog(Integer sopId) {

        SopFileDetails sopFileDetails = sopFileRepo.findById(sopId)
                .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopId));
        return this.logRepo.getViewerActivityLog(sopId, sopFileDetails.getSopVersion());
    }

    @Override
    public ResponseEntity<ViewSopDetailsInterfaceDTO> getEndorsedSOP(String empId) {
        try {
            User userDetails = commonService.getUserDtls(empId);

            ViewSopDetailsInterfaceDTO viewSopDetailsInterfaceDTO = sopFileRepo.getEndorsedSOP(userDetails.getSectionMaster().getId());
            return ResponseEntity.ok().body(viewSopDetailsInterfaceDTO);
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @Override
    public ResponseEntity<List<ResponsibilityDTO>> getResponsibilityListBySopId(Integer sopId) {

        List<ResponsibilityDTO> responsibilityDTOList = getResponsibilityList(sopId);

        return ResponseEntity.ok().body(responsibilityDTOList);
    }

    @Override
    public ResponseEntity<ResponsibilityDTO> getResponsibilityDtlsByRespId(Integer respId) {
        try {
            List<ResponsibilityInterfaceDTO> responsibilityInterfaceDTOList = responsibilityRepo.viewResponsibilitiesLstDtlsByRespId(respId);

            ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO();

            List<RemarksDTO> remarksDTOList = new ArrayList<>();


            if (!CollectionUtils.isEmpty(responsibilityInterfaceDTOList)) {

                for (ResponsibilityInterfaceDTO responsibilityInterfaceDTO : responsibilityInterfaceDTOList) {

                    List<DocumentInterfaceViewDTO> docList = docuRepo.getDocList(respId);

                    List<SOPAttachmentDetailsDTO> attachmentList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(docList)) {
                        for (DocumentInterfaceViewDTO docDTO : docList) {
                            SOPAttachmentDetailsDTO attachment = new SOPAttachmentDetailsDTO(docDTO.getDocUUID(), docDTO.getDocName());
                            attachmentList.add(attachment);
                        }
                    } else {
                        attachmentList.add(null);
                        }
                    String status;
                    if (responsibilityInterfaceDTO.getRespDelete().charValue() == 'Y'){
                        status = "In-Active";
                    }else {
                        status = "Active";
                    }


                    if(responsibilityInterfaceDTO.getRoleId() == 2){
                        responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(), responsibilityInterfaceDTO.getResponsibilityName(), responsibilityInterfaceDTO.getRoleHolder(),
                                responsibilityInterfaceDTO.getRoleHolderName(), responsibilityInterfaceDTO.getRemarks(), responsibilityInterfaceDTO.getSecList(),
                                responsibilityInterfaceDTO.getSecNameList(),
                                null, responsibilityInterfaceDTO.getContent(), null, null, responsibilityInterfaceDTO.getDeptName(),
                                responsibilityInterfaceDTO.getEffectiveDate(), responsibilityInterfaceDTO.getDepartmentName(), attachmentList, null, status,null, responsibilityInterfaceDTO.getRelatedStatus(),
                                null, null, null);
                    }else {
                        responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(), responsibilityInterfaceDTO.getResponsibilityName(), responsibilityInterfaceDTO.getRoleHolder(),
                                responsibilityInterfaceDTO.getRoleHolderName(), responsibilityInterfaceDTO.getRemarks(), responsibilityInterfaceDTO.getSecList(),
                                responsibilityInterfaceDTO.getSecNameList(),null, responsibilityInterfaceDTO.getContent(), null, null, responsibilityInterfaceDTO.getDeptName(),
                                responsibilityInterfaceDTO.getEffectiveDate(), responsibilityInterfaceDTO.getDepartmentName(), attachmentList, null, status, null,
                                responsibilityInterfaceDTO.getRelatedStatus(), null, null, null);
                    }
                }

                List<RemarkInterface> remarkInterfaceList = responsibilityRepo.getRemarks(respId);

                if (!CollectionUtils.isEmpty(remarkInterfaceList)){

                    for (RemarkInterface remarkInterface : remarkInterfaceList){
                        RemarksDTO remarksDTO = new RemarksDTO();
                        remarksDTO.setRemarks(remarkInterface.getRemarks());
                        remarksDTO.setSecName(remarkInterface.getSecName());
                        remarksDTO.setStatus(remarkInterface.getStatus());

                        remarksDTOList.add(remarksDTO);
                    }
                }

                responsibilityDTO.setRemarksDTOList(remarksDTOList);

            }
            return ResponseEntity.ok().body(responsibilityDTO);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<SopResponse> deleteResponsibility(Integer respId, String actionBy) {
        SopResponse response = new SopResponse();
        try {
        	Responsibilities deleteResponsibility = responsibilityRepo.findById(respId)
                    .orElseThrow(() -> new IllegalArgumentException("activity Details not found with ID: " + respId));

            SopFileDetails sopFileDetails = sopFileRepo.findById(deleteResponsibility.getSopId().getId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + deleteResponsibility.getSopId().getId()));

            responsibilityRepo.deleteSecMappingByRespId(respId);
            responsibilityRepo.deleteDocMappingByRespId(respId);
            responsibilityRepo.deleteFromRelated(respId);
            responsibilityRepo.deleteById(respId);

            var logEntiy = SopLogDetails.builder()
                    .actionTime(LocalDateTime.now())
                    .actionTakenBy(commonService.getUserDtls(actionBy))
                    .sopFileId(sopFileDetails)
                    .sopVersion(sopFileDetails.getSopVersion())
                    .action("Deleted Activity Name "+deleteResponsibility.getResponsibilityName())
                    .build();
            logRepo.save(logEntiy);

            response.setResponseText("Activity Successfully Deleted");
            response.setSuccess(true);
            response.setResponseCode(1);
            response.setSopId(String.valueOf(sopFileDetails.getId()));
            response.setSopName(sopFileDetails.getCreatedBy().getSectionMaster().getSectionName());

        } catch (Exception e) {
            response.setResponseText(e.getMessage());
            response.setResponseText("Activity failed to Deleted");
            response.setSuccess(false);
            response.setResponseCode(0);
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @Override
    public ResponseEntity<SopResponse> deleteFileByUUID(String uuid, String actionBy) {

        SopResponse sopResponse = new SopResponse();
        try {
            Document docDtls = docuRepo.getDocDtlsByUUID(uuid);

            docuRepo.deleteDocByUUID(uuid);

            Responsibilities getResponsibility = responsibilityRepo.findById(docDtls.getRespId().getResId())
                    .orElseThrow(() -> new IllegalArgumentException("activity Details not found with ID: " + docDtls.getRespId().getResId()));

            SopFileDetails sopFileDetails = sopFileRepo.findById(getResponsibility.getSopId().getId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + getResponsibility.getSopId().getId()));

                    var logEntiy = SopLogDetails.builder()
                            .actionTime(LocalDateTime.now())
                            .sopFileId(sopFileDetails)
                            .actionTakenBy(commonService.getUserDtls(actionBy))
                            .sopVersion(sopFileDetails.getSopVersion())
                            .action("Deleted Document with UUID "+uuid)
                            .build();
                    logRepo.save(logEntiy);

            sopResponse.setSuccess(true);
            sopResponse.setResponseText("Successfully deleted the file");
            sopResponse.setResponseCode(1);
        }catch (Exception e){
            sopResponse.setSuccess(false);
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);

            throw new RecordNotFoundException("Error: "+e.getMessage());
        }
        return ResponseEntity.ok(sopResponse);
    }

    @Override
    public ResponseEntity<SopResponse> addResponsibility(RequestNewEndorsementDTO addResponsibilityDTO, MultipartFile[] addFile) {

        SopResponse sopResponse = new SopResponse();
        try {
            User user = commonService.getUserDtls(addResponsibilityDTO.getUpdatedBy());

            SopFileDetails sopFileDetails = sopFileRepo.findById(addResponsibilityDTO.getSopID())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + addResponsibilityDTO.getSopID()));

            if (!CollectionUtils.isEmpty(addResponsibilityDTO.getResponsibilityDTOList())) {
                for (ResponsibilityDTO responsibilityDTO : addResponsibilityDTO.getResponsibilityDTOList()) {

                    if (responsibilityDTO.getRespId() != null){
                        Responsibilities responsibilityName = responsibilityRepo.findById(responsibilityDTO.getRespId())
                                .orElseThrow(() -> new IllegalArgumentException("Activity Details not found with ID: " + responsibilityDTO.getRespId()));

                        responsibilityDTO.setResponsibilityName(responsibilityName.getResponsibilityName());
                    }

                    Responsibilities addResponsibilityEntity = new Responsibilities();

                    addResponsibilityEntity.setResponsibilityName(responsibilityDTO.getResponsibilityName());
                    addResponsibilityEntity.setSopId(sopFileDetails);
                    addResponsibilityEntity.setRemarks(responsibilityDTO.getRemarks());
                    addResponsibilityEntity.setContent(responsibilityDTO.getContent());
                    addResponsibilityEntity.setUpdatedBy(commonService.getUserDtls(addResponsibilityDTO.getUpdatedBy()));
                    addResponsibilityEntity.setUpdatedOn(LocalDateTime.now());
                    addResponsibilityEntity.setTypeId(sopFileDetails.getSopTypeMaster());
                    addResponsibilityEntity.setSopVersion(sopFileDetails.getSopVersion());

                    if (sopFileDetails.getSopTypeMaster().getId() == 2){
                        addResponsibilityEntity.setIsAddendum('Y');
                        addResponsibilityEntity.setIsEndorsed('N');
                    }else {
                        addResponsibilityEntity.setIsAddendum('N');
                        addResponsibilityEntity.setIsEndorsed('N');
                    }

                    Responsibilities respStatus = responsibilityRepo.save(addResponsibilityEntity);

                    List<Responsibility_Section_mapping> responsibilityDepartmentMappingList = new ArrayList<>();
                    List<InterRelatedResponsibility_Mapping> lst = new ArrayList<>();

                    if (responsibilityDTO.getSectionList().length != 0) {
                        for (int i = 0; i < responsibilityDTO.getSectionList().length; i++) {

                            Responsibility_Section_mapping responsibilityDepartmentMapping = new Responsibility_Section_mapping();
                            Integer secID = Integer.valueOf(responsibilityDTO.getSectionList()[i]);

                            Integer dept = responsibilityRepo.getDeptByID(secID);

                            DepartmentMaster departmentMaster = departmentRepo.findById(dept)
                                    .orElseThrow(() -> new IllegalArgumentException("Department Details not found with ID: " + dept));

                            SectionMaster sectionMaster = sectionRepo.findById(secID)
                                    .orElseThrow(() -> new IllegalArgumentException("Department Details not found with ID: " + secID));

                            responsibilityDepartmentMapping.setResponsibilities(getRespById(respStatus.getResId()));
                            responsibilityDepartmentMapping.setDepartmentMasterList(departmentMaster);
                            responsibilityDepartmentMapping.setSectionMaster(sectionMaster);
                            responsibilityDepartmentMapping.setStatus('P');

                            responsibilityDepartmentMappingList.add(responsibilityDepartmentMapping);
                        }

                        resp_sec_mapping_repo.saveAll(responsibilityDepartmentMappingList);

                        for (int i = 0; i < responsibilityDTO.getSectionList().length; i++) {

                            InterRelatedResponsibility_Mapping interRelatedResponsibilityMapping = new InterRelatedResponsibility_Mapping();

                            //To send notification to concern Section Head when there is related sop/resp

                            Integer secID = Integer.valueOf(responsibilityDTO.getSectionList()[i]);

                            SectionMaster sectionMaster = sectionRepo.findById(secID)
                                    .orElseThrow(() -> new IllegalArgumentException("Department Details not found with ID: " + secID));

                            UserInfoInterfaceDTO userInfoInterfaceDTO = userRepo.getRelatedSectionHead(secID, respStatus.getResId());

                            if(responsibilityDTO.getRespId() != null){
                                updateInterDeptActivity(responsibilityDTO.getRespId(), user.getSectionMaster().getId(),respStatus.getResId());
                            }

                            interRelatedResponsibilityMapping.setRespFrom(respStatus);
                            interRelatedResponsibilityMapping.setSecTo(sectionMaster);
                            //inter_resp_mapping_repo.save(interRelatedResponsibilityMapping);

                            lst.add(interRelatedResponsibilityMapping);

                            SendNotificationToRelatedRespSecHead(userInfoInterfaceDTO, user, responsibilityDTO);
                        }
                        inter_resp_mapping_repo.saveAll(lst);
                    }else {
                        if(responsibilityDTO.getRespId() != null){
                            updateInterDeptActivity(responsibilityDTO.getRespId(), user.getSectionMaster().getId(),respStatus.getResId());
                        }
                    }

                    if (addFile.length > 0) {

                        var logEntiy = SopLogDetails.builder()
                                .actionTime(LocalDateTime.now())
                                .actionTakenBy(commonService.getUserDtls(addResponsibilityDTO.getUpdatedBy()))
                                .sopFileId(respStatus.getSopId())
                                .sopVersion(sopFileDetails.getSopVersion())
                                .action("Added New Activity with name "+respStatus.getResponsibilityName())
                                .build();
                        logRepo.save(logEntiy);

                        Boolean uploadDocStatus = updateMultipleDocumentDetails(addFile, addResponsibilityDTO.getUpdatedBy(), addResponsibilityEntity.getResId(), responsibilityDTO.getFileCounter(),sopFileDetails);

                        if (uploadDocStatus) {
                            sopResponse.setResponseCode(1);
                            sopResponse.setResponseText("Activity added successfully");
                            sopResponse.setSuccess(true);
                        }
                    }
                }
            }
    }catch(Exception e){
            sopResponse.setResponseCode(0);
            sopResponse.setResponseText(e.getMessage());
            sopResponse.setSuccess(false);

            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    private void SendNotificationToRelatedRespSecHead(UserInfoInterfaceDTO userInfoInterfaceDTO, User user, ResponsibilityDTO responsibilityDTO) {

        String gender = "";

        if (userInfoInterfaceDTO.getGender().charValue() == 'M'){
            gender = "Mr";
        }else {
            gender = "Ms";
        }

        String mailContent =
                "<html> <body>"+"Dear Sir/Madam,<br><br>"
                        + "The "+user.getSectionMaster().getSectionName()+ " section within the "+user.getDepartmentMaster().getDepartmentShortCode()+ " department has proposed new SOP, which includes a related activity titled \""+responsibilityDTO.getResponsibilityName()+"\" that is associated with your section. Kindly check this activity and ensure its seamless integration and continuation within your SOP. <br>"
                        + "<p>Thank You</p>"
                        +"</body></html>";

        String smsContent =
                "Dear Sir/Madam,\n"
                        + "Kindly check the Inter-related activities in SOP System proposed by "+user.getSectionMaster().getSectionName()+ " and proceed with the appropriate actions.\n"
                        + "Thank You";

        try{
            MailSender.sendMail(userInfoInterfaceDTO.getEmail(), null, null, mailContent, "SOP Related Activity Notification");
            SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
            smsSenderHttp.sendSMS(userInfoInterfaceDTO.getPhoneNo(), smsContent);

        } catch (Exception e) {
            throw  new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<SopResponse> updateResponsibility(ResponsibilityDTO updateResponsibilityDTO, MultipartFile[] updateFile) {

        SopResponse sopResponse = new SopResponse();
        try {

            Responsibilities updateResponsibilities = responsibilityRepo.findById(updateResponsibilityDTO.getRespId())
                    .orElseThrow(() -> new IllegalArgumentException("Activity Details not found with ID: " + updateResponsibilityDTO.getRespId()));

            SopFileDetails sopFileDetails = sopFileRepo.findById(updateResponsibilities.getSopId().getId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + updateResponsibilities.getSopId()));

            updateResponsibilities.setTypeId(sopFileDetails.getSopTypeMaster());

            User user = commonService.getUserDtls(updateResponsibilityDTO.getUpdatedBy());

            if (updateResponsibilityDTO.getResponsibilityName() != null) {
                updateResponsibilities.setResponsibilityName(updateResponsibilityDTO.getResponsibilityName());
            }
            if (updateResponsibilityDTO.getContent() != null) {
                updateResponsibilities.setContent(updateResponsibilityDTO.getContent());
            }
            if (updateResponsibilityDTO.getRemarks() != null) {
                updateResponsibilities.setRemarks(updateResponsibilityDTO.getRemarks());
            }
            if (updateResponsibilityDTO.getUpdatedBy() != null) {
                updateResponsibilities.setUpdatedBy(commonService.getUserDtls(updateResponsibilityDTO.getUpdatedBy()));
            }
            updateResponsibilities.setUpdatedOn(LocalDateTime.now());

            if (sopFileDetails.getSopTypeMaster().getId() == 2){
                updateResponsibilities.setSopVersion(sopFileDetails.getSopVersion());
                updateResponsibilities.setIsAddendum('Y');
            }

            responsibilityRepo.save(updateResponsibilities);

            if (updateResponsibilityDTO.getSectionList() != null) {

                //[] sectionList = updateResponsibilityDTO.getSectionList();

                String[] sectionList = Arrays.stream(updateResponsibilityDTO.getSectionList())
                        .flatMap(s -> Arrays.stream(s.replaceAll("\\[|\\]", "").split(",")))
                        .toArray(String[]::new);

                String[] secList = resp_sec_mapping_repo.getSecList(updateResponsibilityDTO.getRespId());

                List<String> listA = new ArrayList<>(Arrays.asList(sectionList));
                List<String> listB = new ArrayList<>(Arrays.asList(secList));

                List<String> newEntryList = listA.parallelStream()
                        .filter(value -> !listB.contains(value))
                        .collect(Collectors.toList());

                List<String> removeList = listB.parallelStream()
                        .filter(value -> !listA.contains(value))
                        .collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(removeList)){
                    for(String secId : removeList){
                        String status = resp_sec_mapping_repo.getRelatedStatus(updateResponsibilityDTO.getRespId(), Integer.parseInt(secId));
                        if (!status.equalsIgnoreCase("A")){
                            responsibilityRepo.deleteExistingRelatedSecId(secId, updateResponsibilityDTO.getRespId());
                            resp_sec_mapping_repo.deleteExistingSection(secId, updateResponsibilityDTO.getRespId());
                        }
                    }
                }

                List<Integer> checkRelatedStatus = resp_sec_mapping_repo.getStatus(updateResponsibilityDTO.getRespId());

                if (!CollectionUtils.isEmpty(checkRelatedStatus)){
                    for (Integer relid : checkRelatedStatus){

                        resp_sec_mapping_repo.updatePendingStatus(relid);
                    }
                }

                if (!CollectionUtils.isEmpty(newEntryList)){
                    for (String newSecId : newEntryList) {

                        Responsibility_Section_mapping updateRespSecMapping = new Responsibility_Section_mapping();
                        InterRelatedResponsibility_Mapping interRelatedResponsibilityMapping = new InterRelatedResponsibility_Mapping();

                        try {
                            Integer secId = Integer.parseInt(newSecId);
                            UserInfoInterfaceDTO userInfoInterfaceDTO = userRepo.getRelatedSectionHead(secId, updateResponsibilityDTO.getRespId());

                            Integer dept = responsibilityRepo.getDeptByID(secId);

                            DepartmentMaster departmentMaster = departmentRepo.findById(dept)
                                    .orElseThrow(() -> new IllegalArgumentException("Department Details not found with ID: " + dept));

                            SectionMaster sectionMaster = sectionRepo.findById(secId)
                                    .orElseThrow(() -> new IllegalArgumentException("Section Details not found with ID: " + secId));

                            updateRespSecMapping.setResponsibilities(updateResponsibilities);
                            updateRespSecMapping.setDepartmentMasterList(departmentMaster);
                            updateRespSecMapping.setSectionMaster(sectionMaster);
                            updateRespSecMapping.setStatus('P');

                            resp_sec_mapping_repo.save(updateRespSecMapping);

                            interRelatedResponsibilityMapping.setRespFrom(updateResponsibilities);
                            interRelatedResponsibilityMapping.setSecTo(sectionMaster);
                            inter_resp_mapping_repo.save(interRelatedResponsibilityMapping);

                            SendNotificationToRelatedRespSecHead(userInfoInterfaceDTO, user, updateResponsibilityDTO);

                        }catch (Exception e){
                            System.err.println("Error : " + e.getMessage());

                        }
                    }
                }
            }

            if (updateFile.length > 0) {
                var logEntiy = SopLogDetails.builder()
                        .actionTime(LocalDateTime.now())
                        .actionTakenBy(commonService.getUserDtls(updateResponsibilityDTO.getUpdatedBy()))
                        .sopFileId(sopFileDetails)
                        .sopVersion(sopFileDetails.getSopVersion())
                        .action("Updated Activity with name "+updateResponsibilities.getResponsibilityName())
                        .build();
                logRepo.save(logEntiy);

                Boolean uploadDocStatus = updateMultipleDocumentDetails(updateFile, updateResponsibilityDTO.getUpdatedBy(), updateResponsibilityDTO.getRespId(), updateResponsibilityDTO.getFileCounter(), updateResponsibilities.getSopId());

                if (uploadDocStatus) {
                    sopResponse.setResponseText("Activity successfully updated");
                    sopResponse.setResponseCode(1);
                    sopResponse.setSuccess(true);
                }
            }

        }catch (Exception e){

            sopResponse.setResponseCode(0);
            sopResponse.setResponseText(e.getMessage());
            sopResponse.setSuccess(false);

            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    private Boolean saveMultipleDocumentDetails(MultipartFile[] file, User createdByUser, SopFileDetails sopId, Responsibilities resId, Integer fileCounter) {
        Boolean docStatus = false;
        try {
            if(file.length > 0){
                //upload the attached file
                for(int i=0; i < file.length; i ++) {
                    String name = file[i].getOriginalFilename();
                    
                    if(!"".equals(name) && name != null) {
                    	String randomUUID = UUID.randomUUID().toString();
                        String uuid = randomUUID.replaceAll("-", "");

                        String counter[] = name.split("_");
                        Integer counterCheck = Integer.valueOf(counter[0]);

                        ResourceBundle resourceBundle = ResourceBundle.getBundle("documentUploads");
                        String filePathPrefix = resourceBundle.getString("uploadFilePath");
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM");
                        String urlAppender = "/" + calendar.get(Calendar.YEAR) + "/" + dateFormat.format(calendar.getTime()) + "/" + calendar.get(Calendar.DATE) + "/";
                        String filePath = filePathPrefix + urlAppender + uuid + "_" + name;
                        File fileloc = new File(urlAppender);
                        if (!fileloc.exists()) {
                            new File(filePathPrefix + urlAppender).mkdirs();
                        }
                        if(name != null) {
                            FileCopyUtils.copy(file[i].getBytes(), new File(filePath));

                            if(counterCheck == fileCounter){
                                var docEntity = Document.builder()
                                        .document_name(counter[1])
                                        .document_type(file[i].getContentType())
                                        .filePath(filePath)
                                        .uuid(randomUUID)
                                        .respId(resId)
                                        .build();

                                var saveDoc = docuRepo.save(docEntity);

                                if (saveDoc != null) {
                                    docStatus = true;
                                    var logEntiy = SopLogDetails.builder()
                                            .actionTime(LocalDateTime.now())
                                            .actionTakenBy(createdByUser)
                                            .sopFileId(sopId)
                                            .sopVersion(sopId.getSopVersion())
                                            .action("Uploaded New File with fileName "+counter[1])
                                            .build();
                                    logRepo.save(logEntiy);
                                }
                            }
                        }
                    }
                    else {
                    	docStatus = true;
                    	continue;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return docStatus;
    }

    private Boolean updateMultipleDocumentDetails(MultipartFile[] file, String updatedBy, Integer respId, Integer fileCounter, SopFileDetails sopId) {
        String uuid = "", filePath = "";
        Boolean docStatus = false;
        try {
            if(file.length > 0){
                //upload the attached file
                for(int i=0; i < file.length; i ++) {
                    String name = file[i].getOriginalFilename();
                    
                    if(!"".equals(name) && name != null) {
                    	String randomUUID = UUID.randomUUID().toString();
                        uuid = randomUUID.replaceAll("-", "");

                        ResourceBundle resourceBundle = ResourceBundle.getBundle("documentUploads");
                        String filePathPrefix = resourceBundle.getString("uploadFilePath");
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM");
                        String urlAppender = "/" + calendar.get(Calendar.YEAR) + "/" + dateFormat.format(calendar.getTime()) + "/" + calendar.get(Calendar.DATE) + "/";
                        filePath = filePathPrefix + urlAppender + uuid + "_" + name;
                        File fileloc = new File(urlAppender);
                        if (!fileloc.exists()) {
                            new File(filePathPrefix + urlAppender).mkdirs();
                        }
                        if(name != null) {
                            FileCopyUtils.copy(file[i].getBytes(), new File(filePath));

                            if(fileCounter != null){
                                String counter[] = name.split("_");
                                Integer counterCheck = Integer.valueOf(counter[0]);
                                if(counterCheck == fileCounter){
                                    var docEntity = Document.builder()
                                            .document_name(counter[1])
                                            .document_type(file[i].getContentType())
                                            .filePath(filePath)
                                            .uuid(randomUUID)
                                            .respId(getRespById(respId))
                                            .updatedOn(LocalDateTime.now())
                                            .editedBy(commonService.getUserDtls(updatedBy))
                                            .build();

                                    var saveDoc = docuRepo.save(docEntity);

                                    if (saveDoc != null) {
                                        docStatus = true;
                                        var logEntiy = SopLogDetails.builder()
                                                .actionTime(LocalDateTime.now())
                                                .actionTakenBy(commonService.getUserDtls(updatedBy))
                                                .sopFileId(sopId)
                                                .sopVersion(sopId.getSopVersion())
                                                .action("Uploaded New File with fileName "+counter[1])
                                                .build();
                                        logRepo.save(logEntiy);
                                    }
                                }
                            }else{
                                var docEntity = Document.builder()
                                        .document_name(name)
                                        .document_type(file[i].getContentType())
                                        .filePath(filePath)
                                        .uuid(randomUUID)
                                        .respId(getRespById(respId))
                                        .updatedOn(LocalDateTime.now())
                                        .editedBy(commonService.getUserDtls(updatedBy))
                                        .build();

                                var saveDoc = docuRepo.save(docEntity);

                                if (saveDoc != null) {
                                    docStatus = true;
                                    var logEntiy = SopLogDetails.builder()
                                            .actionTime(LocalDateTime.now())
                                            .actionTakenBy(commonService.getUserDtls(updatedBy))
                                            .sopFileId(sopId)
                                            .action("Uploaded New File with fileName "+name)
                                            .build();
                                    logRepo.save(logEntiy);
                                }
                            }
                        }
                    } else {
                    	docStatus = true;
                    	continue;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return docStatus;
    }

    private Responsibilities getRespById(Integer respId) {
        Responsibilities respEntityId = responsibilityRepo.findById(respId)
                .orElseThrow(() -> new IllegalArgumentException("Activity Details not found with ID: " +respId));
        return respEntityId;
    }

    @Override
    public ResponsibilityDTO getRelatedRespDtls(Integer respId) {
        Responsibilities responsibilities = getRespById(respId);

        ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO();
        responsibilityDTO.setRespId(responsibilities.getResId());
        responsibilityDTO.setResponsibilityName(responsibilities.getResponsibilityName());
        responsibilityDTO.setContent(responsibilities.getContent());

        return responsibilityDTO;
    }

    @Override
    public void updateRelatedStatus(Integer respId, String userId) {

        User userDetails = commonService.getUserDtls(userId);
        if(respId != null && userId != null){
            resp_sec_mapping_repo.updateRelatedStatus(respId, userDetails.getSectionMaster().getId());
        }
    }

    @Override
    public void updateRelatedRemarks(Integer respId, String userId, String remarks) {

        Responsibilities responsibilities = getRespById(respId);
        SopFileDetails sopFileDetails = sopFileRepo.findById(responsibilities.getSopId().getId())
                .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + responsibilities.getSopId()));

        User userDetails = commonService.getUserDtls(userId);

        if(respId != null && userId != null){
            resp_sec_mapping_repo.updateRelatedRemarks(remarks, respId, userDetails.getSectionMaster().getId());
        }

        String gender = "";

        if (sopFileDetails.getCreatedBy().getGender().charValue() == 'M'){
            gender = "Mr";
        }else {
            gender = "Ms";
        }

        String fullName = sopFileDetails.getCreatedBy().getFirstName();

        String activityName = responsibilities.getResponsibilityName();

        String mailContent =
                "<html> <body>"+"Dear Sir/Madam,<br>"
                        + "The Activity named "+activityName+ " of your section, which was related to "+userDetails.getSectionMaster().getSectionName()+ " section has been disagreed by the author, kindly check the remarks and take appropriate actions.<br>"
                        + "<p>Thank You</p>"
                        +"</body></html>";

        String smsContent =
                "Dear Sir/Madam,\n"
                        + "The Activity named "+activityName+ " of your section, which was related to "+userDetails.getSectionMaster().getSectionName()+ " section has been disagreed by the author, kindly check the remarks and take appropriate actions.\n"
                        + "Thank You";
        try{

            MailSender.sendMail(sopFileDetails.getCreatedBy().getEmailId(), null, null, mailContent, "SOP Related Activity Notification");
            SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
            smsSenderHttp.sendSMS(sopFileDetails.getCreatedBy().getMobileNo(), smsContent);

        } catch (Exception e) {
            throw  new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getRelatedRemarks(Integer respId) {
        try {
            List<ViewSopDetailsInterfaceDTO> relatedRemarkList = resp_sec_mapping_repo.getRelatedRemarks(respId);
            return ResponseEntity.ok().body(relatedRemarkList);
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<ResponsibilityDTO> getRelatedActivityDetails(Integer respId, String user) {
        try {
            List<ResponsibilityInterfaceDTO> responsibilityInterfaceDTOList = responsibilityRepo.viewResponsibilitiesLstDtlsByRespId(respId);

            User userDetails = commonService.getUserDtls(user);

            String relatedStatus = resp_sec_mapping_repo.getRelatedStatus(respId, userDetails.getSectionMaster().getId());

            ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO();
            if (!CollectionUtils.isEmpty(responsibilityInterfaceDTOList)) {

                for (ResponsibilityInterfaceDTO responsibilityInterfaceDTO : responsibilityInterfaceDTOList) {

                    List<DocumentInterfaceViewDTO> docList = docuRepo.getDocList(respId);

                    List<SOPAttachmentDetailsDTO> attachmentList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(docList)) {
                        for (DocumentInterfaceViewDTO docDTO : docList) {
                            SOPAttachmentDetailsDTO attachment = new SOPAttachmentDetailsDTO(docDTO.getDocUUID(), docDTO.getDocName());
                            attachmentList.add(attachment);
                        }

                    } else {
                        attachmentList.add(null);
                    }
                    String status;
                    if (responsibilityInterfaceDTO.getRespDelete().charValue() == 'Y'){
                        status = "In-Active";
                    }else {
                        status = "Active";
                    }

                    if(responsibilityInterfaceDTO.getRoleId() == 2){
                        responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(), responsibilityInterfaceDTO.getResponsibilityName(), responsibilityInterfaceDTO.getRoleHolder(),
                                responsibilityInterfaceDTO.getRoleHolderName(), responsibilityInterfaceDTO.getRemarks(), responsibilityInterfaceDTO.getSecList(), responsibilityInterfaceDTO.getSecNameList(),
                                null, responsibilityInterfaceDTO.getContent(), null, null, responsibilityInterfaceDTO.getDeptName(),
                                responsibilityInterfaceDTO.getEffectiveDate(), responsibilityInterfaceDTO.getDepartmentName(), attachmentList, null, status,null, relatedStatus,
                                null, null, null);
                    }else {
                        responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(), responsibilityInterfaceDTO.getResponsibilityName(), responsibilityInterfaceDTO.getRoleHolder(),
                                responsibilityInterfaceDTO.getRoleHolderName(), responsibilityInterfaceDTO.getRemarks(), responsibilityInterfaceDTO.getSecList(), responsibilityInterfaceDTO.getSecNameList(),
                                null, responsibilityInterfaceDTO.getContent(), null, null, responsibilityInterfaceDTO.getDeptName(),
                                responsibilityInterfaceDTO.getEffectiveDate(), responsibilityInterfaceDTO.getDepartmentName(), attachmentList, null, status, null,
                                relatedStatus, null, null, null);
                    }
                }
            }
            return ResponseEntity.ok().body(responsibilityDTO);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<SopResponse> updateIntro(Integer sopId, String introduction, String updatedBy) {
        SopResponse response = new SopResponse();
        try {
            if (sopId != null) {
                SopFileDetails updateSopEntity = sopFileRepo.findById(sopId)
                        .orElseThrow(() -> new IllegalArgumentException("SOP Type ID not found with ID: " + sopId));

                if (updateSopEntity != null) {

                    if (updatedBy != null) {
                        updateSopEntity.setUpdatedBy(commonService.getUserDtls(updatedBy));
                    }
                    if (introduction != null) {
                        updateSopEntity.setIntroduction(introduction);
                    }
                    updateSopEntity.setUpdatedOn(LocalDateTime.now());

                    sopFileRepo.save(updateSopEntity);
                }
                response.setResponseText("Introduction Updated Successfully");
                response.setSuccess(true);
                response.setResponseCode(200);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok().body(response);
    }


}
