package com.tashicell.sop.Service;

import com.tashicell.sop.Enum.StageMasterEnum;
import com.tashicell.sop.Modal.*;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.*;
import com.tashicell.sop.Utility.MailSender;
import com.tashicell.sop.Utility.SMSSenderHttp;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional

public class FocalPersonServiceImpl implements FocalPersonService{

    private final CommonServiceImpl commonService;

    private final SopStageRepo sopStageRepo;

    private final SopFileRepo sopFileRepo;

    private final LogRepo logRepo;

    private final SopWorkFLowRepo workFLowRepo;

    private final UserRepo userRepo;

    private final EndorsedSopDocRepo sopDocRepo;
    private final ChangeHistoryRepo changeHistoryRepo;
    @Override
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getFocalTaskList() {

        StageMaster stageMaster = sopStageRepo.findById(StageMasterEnum.focalPersonReview.getStageId())
                .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.focalPersonReview.getStageId()));
        List<ViewSopDetailsInterfaceDTO> viewSopDetailsInterfaceDTOList = sopFileRepo.getFocalTaskList(stageMaster.getId());
        return ResponseEntity.ok().body(viewSopDetailsInterfaceDTOList);
    }

    @Override
    public ResponseEntity<SopResponse> submitForReview(Integer sopFileId, String updatedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        Integer roleId = 0;
        try {
            StageMaster stageMaster = sopStageRepo.findById(StageMasterEnum.reviewer_verify.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.initiated.getStageId()));

            User user = commonService.getUserDtls(updatedBy);

            SopFileDetails sopFileDetails = sopFileRepo.findById(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));

            WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopFileId));

            if (workFlowDetails != null) {
                workFlowDetails.setStageMaster(stageMaster);
                workFlowDetails.setActionTakenBy(user);
                workFlowDetails.setRemarks(remark);
                workFlowDetails.setActionTakenOn(LocalDateTime.now());
                workFlowDetails.setSopVersion(sopFileDetails.getSopVersion());

                workFLowRepo.save(workFlowDetails);

                var logEntity = SopLogDetails.builder()
                        .actionTime(LocalDateTime.now())
                        .actionTakenBy(user)
                        .sopFileId(sopFileDetails)
                        .sopVersion(sopFileDetails.getSopVersion())
                        .remarks(remark)
                        .action("Approved by Focal and Submitted for Review")
                        .build();
                logRepo.save(logEntity);

                if((sopFileDetails.getDeptID().getId() == 12) || (sopFileDetails.getDeptID().getId() == 13)){
                    roleId = 7;
                }else{
                    roleId = 4;
                }
                User reviewerDtls = userRepo.getReviewerDtls(sopFileDetails.getDeptID().getId(), roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Reviewer Details not found with DeptID: " + sopFileDetails.getDeptID().getId()));;

                String gender = "";


                if (reviewerDtls.getGender().charValue() == 'M'){
                    gender = "Mr";
                }else {
                    gender = "Ms";
                }

                String fullName = reviewerDtls.getFirstName() + reviewerDtls.getLastName();

                String mailContent =
                        "<html> <body>"+"Dear "+gender+" "+fullName+",<br><br>"
                                + "The "+sopFileDetails.getSecID().getSectionName()+ " section within the "+sopFileDetails.getDeptID().getDepartmentShortCode()+" department has submitted a proposed SOP for your review. Please kindly proceed with the appropriate actions.<br>"
                                + "<p>Thank You</p>"
                                +"</body></html>";

                String smsContent =
                        "Dear "+gender+" "+fullName+",\n"
                                + "The "+sopFileDetails.getSecID().getSectionName()+ " section within the "+sopFileDetails.getDeptID().getDepartmentShortCode()+" department has submitted a proposed SOP for your review. Please kindly proceed with the appropriate actions.\n"
                                + "Thank You";
                try{

                    MailSender.sendMail(reviewerDtls.getEmailId(), null, null, mailContent, "SOP Review Notification");
                    SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
                    smsSenderHttp.sendSMS(reviewerDtls.getMobileNo(), smsContent);

                } catch (Exception e) {
                    sopResponse.setResponseText(e.getMessage());
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
                    throw  new RuntimeException(e.getMessage());
                }


                sopResponse.setResponseText("Successfully Submitted SOP for Review to Head");
                sopResponse.setSuccess(true);
                sopResponse.setResponseCode(1);
            } else {
                sopResponse.setResponseText("Failed to Submitted to Head SOP for Review");
                sopResponse.setSuccess(false);
                sopResponse.setResponseCode(0);
            }
        } catch (Exception e) {
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    @Override
    public ResponseEntity<SopResponse> rejectedByFocalPerson(Integer sopFileId, String reviewedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        try {
            StageMaster stageMaster = sopStageRepo.findById(StageMasterEnum.focalPerson_rejected.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.focalPerson_rejected.getStageId()));

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
                    .action("Rejected SOP by Focal Person, SPPD")
                    .build();
            logRepo.save(logEntity);

            Integer secId = sopFileDetails.getCreatedBy().getSectionMaster().getId();
            Integer roleId = sopFileDetails.getCreatedBy().getRole().getId();

            UserInfoInterfaceDTO authorInfoDetails = userRepo.getAuthorDetails(secId, roleId);

            if (authorInfoDetails != null){

                String gender = "";
                if (authorInfoDetails.getGender().charValue() == 'M'){
                    gender = "Mr";
                }else {
                    gender = "Ms";
                }

                String fullName = authorInfoDetails.getUserName();

                String mailContent =
                        "<html> <body>"+"Dear "+gender+ " "+fullName+",<br><br>"
                                + "The Proposed SOP of your section has been rejected by the focal person. Kindly proceed with the appropriate actions.<br>"
                                + "<p>Thank You</p>"
                                +"</body></html>";

                String smsContent =
                        "Dear "+gender+ " "+fullName+",\n"
                                + "The Proposed SOP of your section has been rejected by the focal person. Kindly proceed with the appropriate actions.\n"
                                + "Thank You";
                try {
                    MailSender.sendMail(sopFileDetails.getCreatedBy().getEmailId(), null, null, mailContent, "SOP Rejection by Focal Person");
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
    public ResponseEntity<List<DocumentInterfaceViewDTO>> getSopHistoryByFocal(Integer dept_Id) {
        List<DocumentInterfaceViewDTO> sopList = sopDocRepo.getSopHistoryByFocal(dept_Id);
        return ResponseEntity.ok().body(sopList);
    }

    @Override
    public DocumentInterfaceViewDTO downloadEndorsedSop(String docUUID) {
        return this.sopDocRepo.downloadEndorsedSop(docUUID);
    }

    @Override
    public List<ViewStatusInterface> getViewStatus(Integer stageID, Integer deptId) {

        if (stageID == 9){

            if (deptId == 14){
                List<ViewStatusInterface> viewStatusInterfaceList = changeHistoryRepo.getAllDeptPendingList(stageID);
                return viewStatusInterfaceList;
            }else{
                List<ViewStatusInterface> viewStatusInterfaceList = changeHistoryRepo.getPendingList(stageID, deptId);
                return viewStatusInterfaceList;
            }

        }else {
            if (deptId == 14){
                List<ViewStatusInterface> viewStatusInterfaceList = changeHistoryRepo.getAllStatusList(stageID);
                return viewStatusInterfaceList;
            }else{
                List<ViewStatusInterface> viewStatusInterfaceList = changeHistoryRepo.getEndorserStatusList(stageID, deptId);
                return viewStatusInterfaceList;
            }

        }
    }

    @Override
    public List<ViewStatusInterface> getAllStatusDetails() {

        List<ViewStatusInterface> viewStatusInterfaceList = changeHistoryRepo.getAllStatusDetails();
        return viewStatusInterfaceList;
    }

    @Override
    public List<ViewStatusInterface> getEndorserPendingList(Integer sopId) {
        List<ViewStatusInterface> viewStatusInterfaceList = changeHistoryRepo.getEndorserPendingList(sopId);
        return viewStatusInterfaceList;
    }
}
