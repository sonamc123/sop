package com.tashicell.sop.Service;

import ch.qos.logback.core.layout.EchoLayout;
import com.tashicell.sop.Enum.StageMasterEnum;
import com.tashicell.sop.Modal.*;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.*;
import com.tashicell.sop.Utility.MailSender;
import com.tashicell.sop.Utility.SMSSenderHttp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewerServiceImpl implements ReviewerService{
    private final UserRepo userRepo;
    private final SopFileRepo sopFileRepo;
    private final SopStageRepo sopStageRepo;
    private final SopWorkFLowRepo workFLowRepo;

    private final LogRepo logRepo;

    private final EndorserRepo endorserRepo;

    private final CommonServiceImpl commonService;

    private final ResponsibilityRepo responsibilityRepo;

    @Override
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getReviewerTaskList(String empId) {
        User userDetails = commonService.getUserDtls(empId);
        List<ViewSopDetailsInterfaceDTO> viewSopDetailsInterfaceDTOList = sopFileRepo.findSopDtlsByDeptId(userDetails.getDepartmentMaster().getId());
        return ResponseEntity.ok().body(viewSopDetailsInterfaceDTOList);
    }

    @Override
    public ResponseEntity<SopResponse> approveByReviewer(Integer sopFileId, String reviewedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        try {
            StageMaster stageMaster = sopStageRepo.findById(StageMasterEnum.reviewer_approve.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.reviewer_approve.getStageId()));

            StageMaster endorserPending = sopStageRepo.findById(StageMasterEnum.pending.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.endorser_approve.getStageId()));

            StageMaster endorserEndorsed = sopStageRepo.findById(StageMasterEnum.endorser_approve.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.endorser_approve.getStageId()));

            User user = commonService.getUserDtls(reviewedBy);

            SopFileDetails sopFileDetails= sopFileRepo.findById(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));

            WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopFileId));

            SectionMaster sectionMaster= commonService.getSecId(sopFileDetails.getSecID().getId());

            DepartmentMaster departmentMaster= commonService.getDeptId(sopFileDetails.getDeptID().getId());

            //Save in task list for each endorser to review it.

            List<UserInfoInterfaceDTO>  userInfoList = userRepo.getEndorserList();

            if (userInfoList != null){
                for(UserInfoInterfaceDTO endorserInterface: userInfoList){

                    EndorserTaskList endorserList = new EndorserTaskList();

                    User endorserId = commonService.getUserDtls(endorserInterface.getEmpId());

                    BigInteger endorserCount = endorserRepo.getEndorserCount(sopFileId, endorserId.getId());

                    if(endorserCount.intValue() > 0){
                        EndorserTaskList getEndorserTask = endorserRepo.findEndorserTaskList(sopFileId, endorserId.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Endorser Task List Details not found with ID: " +sopFileId+" ," +endorserId.getId()));

                        if(user.getId() == endorserId.getId()){
                            getEndorserTask.setStageMaster(endorserEndorsed);
                        }else{
                            getEndorserTask.setStageMaster(endorserPending);
                        }

                        getEndorserTask.setActionTakenOn(LocalDateTime.now());
                        getEndorserTask.setReviewerId(reviewedBy);
                        getEndorserTask.setRemarks(remark);
                        getEndorserTask.setSopVersion(sopFileDetails.getSopVersion());

                        endorserRepo.save(getEndorserTask);

                    }else {
                        endorserList.setSopId(sopFileDetails);

                        if(user.getId() == endorserId.getId()){
                            endorserList.setStageMaster(endorserEndorsed);
                        }else{
                            endorserList.setStageMaster(endorserPending);
                        }
                        endorserList.setEndorserId(endorserId);
                        endorserList.setActionTakenOn(LocalDateTime.now());
                        endorserList.setReviewerId(reviewedBy);
                        endorserList.setRemarks(remark);
                        endorserList.setSopVersion(sopFileDetails.getSopVersion());
                        endorserRepo.save(endorserList);
                    }

                    /*String gender = "";
                    if (endorserInterface.getGender().charValue() == 'M'){
                        gender = "Mr";
                    }else {
                        gender = "Ms";
                    }*/

                    if(user.getId() != endorserId.getId()){
                        String endorserMailContent =
                                "<html> <body>"+"Dear Sir/Madam,<br>"
                                        + "The "+sectionMaster.getSectionName()+ " section within "+departmentMaster.getDepartmentShortCode()+ " department has submitted a proposed SOP for your review which has been approved by a Reviewer. Please kindly proceed with the appropriate actions.<br>"
                                        + "<p>Thank You</p>"
                                        +"</body></html>";

                        String smsContent =
                                "Dear Sir/Madam,\n"
                                        + "Proposed SOP of "+sectionMaster.getSectionName()+ " section within the "+departmentMaster.getDepartmentShortCode()+ " department has been approved by a reviewer, Please kindly proceed with the appropriate actions.\n"
                                        + "Thank You";
                        try{
                            MailSender.sendMail(endorserInterface.getEmail(), null, null, endorserMailContent, "SOP Endorsement Notification");
                            SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
                            smsSenderHttp.sendSMS(endorserInterface.getPhoneNo(), smsContent);
                        } catch (Exception e) {
                            sopResponse.setResponseText(e.getMessage());
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
                            throw  new RuntimeException(e.getMessage());
                        }
                    }
                }


                commonService.getWorkFlowDtls(remark, stageMaster, user, sopFileDetails, workFlowDetails, workFLowRepo, logRepo);

                var logEntity = SopLogDetails.builder()
                        .actionTime(LocalDateTime.now())
                        .actionTakenBy(user)
                        .sopFileId(sopFileDetails)
                        .sopVersion(sopFileDetails.getSopVersion())
                        .remarks(remark)
                        .action("Approved by Reviewer")
                        .build();
                logRepo.save(logEntity);

                sopResponse.setResponseText("Approved by Reviewer");
                sopResponse.setSuccess(true);
                sopResponse.setResponseCode(1);

            }else {
                sopResponse.setResponseText("Endorser details not found");
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
            }

        }catch (Exception e){
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    @Override
    public ResponseEntity<SopResponse> rejectedByReview(Integer sopFileId, String reviewedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        try {
            StageMaster stageMaster = sopStageRepo.findById(StageMasterEnum.reviewer_reject.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.reviewer_reject.getStageId()));

            User user = commonService.getUserDtls(reviewedBy);

            SopFileDetails sopFileDetails= sopFileRepo.findById(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));

            WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopFileId));

            commonService.getWorkFlowDtls(remark, stageMaster, user, sopFileDetails, workFlowDetails, workFLowRepo, logRepo);

            var logEntity = SopLogDetails.builder()
                    .actionTime(LocalDateTime.now())
                    .actionTakenBy(user)
                    .sopFileId(sopFileDetails)
                    .sopVersion(sopFileDetails.getSopVersion())
                    .remarks(remark)
                    .action("Rejected SOP by Reviewer")
                    .build();
            logRepo.save(logEntity);

            Integer secId = sopFileDetails.getCreatedBy().getSectionMaster().getId();
            Integer roleId = sopFileDetails.getCreatedBy().getRole().getId();

            UserInfoInterfaceDTO authorInfoDetails = userRepo.getAuthorDetails(secId, roleId);

            if (authorInfoDetails != null) {

                String gender = "";
                if (authorInfoDetails.getGender().charValue() == 'M') {
                    gender = "Mr";
                } else {
                    gender = "Ms";
                }

                String fullName = authorInfoDetails.getUserName();

                String mailContent =
                        "<html> <body>"+"Dear Sir/Madam,<br>"
                                + "The Proposed SOP of your section has been rejected by the reviewer. Kindly proceed with the appropriate actions.<br>"
                                + "<p>Thank You</p>"
                                +"</body></html>";

                String smsContent =
                        "Dear Sir/Madam,\n"
                                + "The Proposed SOP of your section has been rejected by the reviewer. Kindly proceed with the appropriate actions.\n"
                                + "Thank You";
                try {
                    MailSender.sendMail(sopFileDetails.getCreatedBy().getEmailId(), null, null, mailContent, "SOP Rejection by Reviewer");
                    SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
                    smsSenderHttp.sendSMS(sopFileDetails.getCreatedBy().getMobileNo(), smsContent);
                } catch (Exception e) {
                    sopResponse.setResponseText(e.getMessage());
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
                    throw  new RuntimeException(e.getMessage());
                }
            }

            sopResponse.setResponseText("Rejected by Reviewer");
            sopResponse.setSuccess(true);
            sopResponse.setResponseCode(1);

        }catch (Exception e){
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    @Override
    public ResponseEntity<List<ResponsibilityDTO>> getResponsibilityListByReviewer(Integer sopFileId) {
        List<ResponsibilityDTO> responsibilityDTOList = new ArrayList<>();

        try {
            SopFileDetails sopFileDetails= sopFileRepo.findById(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));

            List<ResponsibilityInterfaceDTO> responsibilityList = responsibilityRepo.viewResponsibilitiesLstByVerifier(sopFileId, sopFileDetails.getSopTypeMaster().getId());

            for(ResponsibilityInterfaceDTO responsibilityInterfaceDTO : responsibilityList){

                String status;
                if (responsibilityInterfaceDTO.getRespDelete().charValue() == 'Y'){
                    status = "In-Active";
                }else {
                    status = "Active";
                }

                ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(), responsibilityInterfaceDTO.getResponsibilityName(), null,
                        responsibilityInterfaceDTO.getRoleHolderName(), null, responsibilityInterfaceDTO.getSecList(), null,
                        null, null, null, null, responsibilityInterfaceDTO.getDeptName(), responsibilityInterfaceDTO.getEffectiveDate(),
                        responsibilityInterfaceDTO.getDepartmentName(), null, null, status,null, null, null, null, null);

                responsibilityDTOList.add(responsibilityDTO);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok().body(responsibilityDTOList);
    }

    @Override
    public ResponseEntity<SopResponse> updateContentByReviewer(Integer respId, String respContent, String updatedBy) {
        SopResponse sopResponse = new SopResponse();
        try {

            Responsibilities updateRespContent = responsibilityRepo.findById(respId)
                    .orElseThrow(() -> new IllegalArgumentException("responsibility Details not found with ID: " + respId));

            SopFileDetails sopFileDetails = sopFileRepo.findById(updateRespContent.getSopId().getId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + updateRespContent.getSopId()));

                updateRespContent.setContent(respContent);
                updateRespContent.setUpdatedBy(commonService.getUserDtls(updatedBy));
                updateRespContent.setUpdatedOn(LocalDateTime.now());

                responsibilityRepo.save(updateRespContent);

                var logEntiy = SopLogDetails.builder()
                        .actionTime(LocalDateTime.now())
                        .actionTakenBy(commonService.getUserDtls(updatedBy))
                        .sopFileId(sopFileDetails)
                        .sopVersion(sopFileDetails.getSopVersion())
                        .action("Updated Activity with name "+updateRespContent.getResponsibilityName())
                        .build();
                logRepo.save(logEntiy);

            sopResponse.setResponseText("Activity successfully updated");
            sopResponse.setResponseCode(1);
            sopResponse.setSuccess(true);

        }catch (Exception e){

            sopResponse.setResponseCode(0);
            sopResponse.setResponseText(e.getMessage());
            sopResponse.setSuccess(false);

            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }
}
