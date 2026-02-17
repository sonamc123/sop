package com.tashicell.sop.Service;


import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.tashicell.sop.Enum.StageMasterEnum;
import com.tashicell.sop.Modal.*;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.*;
import com.tashicell.sop.Utility.MailSender;
import com.tashicell.sop.Utility.SMSSenderHttp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class EndorserServiceImpl implements EndorserService{
    private final SopFileRepo sopFileRepo;
    private final SopStageRepo sopStageRepo;

    private final SopWorkFLowRepo workFLowRepo;

    private final LogRepo logRepo;

    private final CommonServiceImpl commonService;

    private final EndorserRepo endorserRepo;

    private final ChangeHistoryRepo changeHistoryRepo;

    private final UserRepo userRepo;

    private final ResponsibilityRepo responsibilityRepo;

    private final SopTypeMasterRepo sopTypeMasterRepo;

    private final ViewerServiceImpl viewerService;

    private final TemplateEngine templateEngine;

    private final EndorsedSopDocRepo endorsedSopDocRepo;

    private final Inter_Resp_Mapping_Repo inter_resp_mapping_repo;


    @Override
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getEndorserTaskList(String empId) {
        List<ViewSopDetailsInterfaceDTO> viewSopDetailsInterfaceDTOList = new ArrayList<>();
        try {
            User userDetails = commonService.getUserDtls(empId);

            if (userDetails != null){
                StageMaster pendingStage = sopStageRepo.findById(StageMasterEnum.pending.getStageId())
                        .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.pending.getStageId()));

                viewSopDetailsInterfaceDTOList = sopFileRepo.findSopDtlsByEndorser(userDetails.getId(), pendingStage.getId());
            }

        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok().body(viewSopDetailsInterfaceDTOList);

    }

    @Override
    public ResponseEntity<SopResponse> approveByEndorser(Integer sopFileId, String approvedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        try {
            StageMaster approveStage = sopStageRepo.findById(StageMasterEnum.endorser_approve.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.endorser_approve.getStageId()));

            StageMaster endorseStage = sopStageRepo.findById(StageMasterEnum.endorser_endorsed.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.endorser_approve.getStageId()));

            User user = commonService.getUserDtls(approvedBy);


            endorserRepo.updateApprovedStatus(approveStage.getId(), remark, sopFileId, user.getId());

            SopFileDetails sopFileDetails= sopFileRepo.findById(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));


            WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopFileId));

            BigInteger approvalCountCheck = endorserRepo.getApprovalCount(sopFileId);

            if (approvalCountCheck.intValue() == 0){

                User authorizerDtls = userRepo.getAuthoriserDtls()
                        .orElseThrow(() -> new IllegalArgumentException("Manager Director details not found!!!"));

                commonService.getWorkFlowDtls(remark, endorseStage, user, sopFileDetails, workFlowDetails, workFLowRepo, logRepo);

                String mailContent =
                        "<html> <body>"+"Dear Sir/Madam,"+"<br>"
                                + "The "+sopFileDetails.getSecID().getSectionName()+" section within "+sopFileDetails.getDeptID().getDepartmentShortCode()+" department has submitted a proposed SOP for your review which has been approved by endorser team.Please kindly proceed with the appropriate actions.<br>"
                                + "<p>Thank You</p>"
                                +"</body></html>";

                String smsContent =
                        "Dear Sir/Madam,\n"
                                + "The Proposed SOP of "+sopFileDetails.getCreatedBy().getSectionMaster().getSectionName()+" section under "+sopFileDetails.getCreatedBy().getDepartmentMaster().getDepartmentShortCode()+" department has been approved by endorser team, Please kindly proceed with the appropriate actions.\n"
                                + "Thank You";
                try{
                    MailSender.sendMail(authorizerDtls.getEmailId(), null, null, mailContent, "SOP Authorisation Notification");
                    SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
                    smsSenderHttp.sendSMS(authorizerDtls.getMobileNo(), smsContent);
                } catch (Exception e) {
                    sopResponse.setResponseText(e.getMessage());
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
                    throw  new RuntimeException(e.getMessage());
                }


            }else {
                sopResponse.setResponseText("Failed to Approve");
                sopResponse.setSuccess(false);
            }


            var logEntity = SopLogDetails.builder()
                    .actionTime(LocalDateTime.now())
                    .actionTakenBy(user)
                    .sopFileId(sopFileDetails)
                    .sopVersion(sopFileDetails.getSopVersion())
                    .remarks(remark)
                    .action("Approved by Endorser")
                    .build();
            logRepo.save(logEntity);

            sopResponse.setResponseText("Approved by Endorser");
            sopResponse.setSuccess(true);
            sopResponse.setResponseCode(1);


        }catch (Exception e){
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    @Override
    public ResponseEntity<SopResponse> rejectByEndorser(Integer sopFileId, String rejectedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        try {
            User user = commonService.getUserDtls(rejectedBy);

            StageMaster rejectStageMaster = new StageMaster();
            if (user.getRole().getId().equals(6)){
                rejectStageMaster = sopStageRepo.findById(StageMasterEnum.authorizer_rejected.getStageId())
                        .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.authorizer_rejected.getStageId()));
            }else {
                rejectStageMaster = sopStageRepo.findById(StageMasterEnum.endorser_reject.getStageId())
                        .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.endorser_reject.getStageId()));
            }

            SopFileDetails sopFileDetails= sopFileRepo.findById(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));

            WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopFileId));

            if (user.getRole().getId().equals(4)){
                endorserRepo.updateRejectStatus(rejectStageMaster.getId(), remark, sopFileId);
            }

            String reviewerId = endorserRepo.getReviewerId(sopFileId);

            User reviewerDtls = commonService.getUserDtls(reviewerId);

            commonService.getWorkFlowDtls(remark, rejectStageMaster, user, sopFileDetails, workFlowDetails, workFLowRepo, logRepo);

            SopLogDetails sopLogDetails = new SopLogDetails();
            sopLogDetails.setSopFileId(sopFileDetails);
            sopLogDetails.setActionTime(LocalDateTime.now());
            sopLogDetails.setActionTakenBy(user);
            sopLogDetails.setSopVersion(sopFileDetails.getSopVersion());
            sopLogDetails.setRemarks(remark);

            if (user.getRole().getId().equals(6)){
                sopLogDetails.setAction("Rejected by Authorizer");
            }else {
                sopLogDetails.setAction("Rejected by Endorser");
            }

            logRepo.save(sopLogDetails);


            String gender = "";
            if (reviewerDtls.getGender().charValue() == 'M'){
                gender = "Mr";
            }else {
                gender = "Ms";
            }
            String fullName = reviewerDtls.getFirstName() + reviewerDtls.getLastName();

            try{
                if (user.getRole().getId().equals(6)){
                    String mailContent =
                            "<html> <body>"+"Dear Sir/Madam,<br>"
                                    + "The Proposed SOP of the "+sopFileDetails.getCreatedBy().getSectionMaster().getSectionName()+" section under your department has been rejected by the Managing Director. Kindly proceed with the appropriate actions.<br>"
                                    + "<p>Thank You</p>"
                                    +"</body></html>";

                    String smsContent =
                            "Dear Sir/Madam,\n"
                                    + "The Proposed SOP of the "+sopFileDetails.getCreatedBy().getSectionMaster().getSectionName()+" section under your department has been rejected by the Managing Director. Kindly proceed with the appropriate actions.\n"
                                    + "Thank You";
                    MailSender.sendMail(reviewerDtls.getEmailId(), null, null, mailContent, "SOP Rejection by Authoriser");
                    SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
                    smsSenderHttp.sendSMS(reviewerDtls.getMobileNo(), smsContent);
                }else{
                    String mailContent =
                            "<html> <body>"+"Dear Sir/Madam,<br>"
                                    + "The Proposed SOP of the "+sopFileDetails.getCreatedBy().getSectionMaster().getSectionName()+" section under your department has been rejected by the endorser. Kindly proceed with the appropriate actions.<br>"
                                    + "<p>Thank You</p>"
                                    +"</body></html>";

                    String smsContent =
                            "Dear Sir/Madam,\n"
                                    + "The Proposed SOP of the "+sopFileDetails.getCreatedBy().getSectionMaster().getSectionName()+" section under your department has been rejected by the endorser. Kindly proceed with the appropriate actions.\n"
                                    + "Thank You";
                    MailSender.sendMail(reviewerDtls.getEmailId(), null, null, mailContent, "SOP Rejection by Endorser");
                    SMSSenderHttp smsSenderHttp = new SMSSenderHttp();
                    smsSenderHttp.sendSMS(reviewerDtls.getMobileNo(), smsContent);
                }

            } catch (Exception e) {
                sopResponse.setResponseText(e.getMessage());
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
                throw  new RuntimeException(e.getMessage());
            }

            sopResponse.setResponseText("Rejected by Endorser");
            sopResponse.setSuccess(true);
            sopResponse.setResponseCode(1);


        }catch (Exception e){
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }
        return ResponseEntity.ok().body(sopResponse);
    }

    @Override
    public ResponseEntity<List<EndorserRemarkInterface>> getEndorserRemark(Integer sopFileId) {
        List<EndorserRemarkInterface> endorserRemarkInterfaces = sopFileRepo.getEndorserRemark(sopFileId);
        return ResponseEntity.ok().body(endorserRemarkInterfaces);
    }


    @Override
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getAuthorizerTaskList() {
        List<ViewSopDetailsInterfaceDTO> viewSopDetailsInterfaceDTOList = new ArrayList<>();
        try {
            StageMaster endorserStage = sopStageRepo.findById(StageMasterEnum.endorser_endorsed.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.endorser_endorsed.getStageId()));

            viewSopDetailsInterfaceDTOList = sopFileRepo.getAuthorizerTaskList(endorserStage.getId());

        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok().body(viewSopDetailsInterfaceDTOList);
    }


    @Override
    public ResponseEntity<SopResponse> approveByAuthorizer(Integer sopFileId, String authorisedBy, String remark) {
        SopResponse sopResponse = new SopResponse();

        try {
            StageMaster authorizerStage = sopStageRepo.findById(StageMasterEnum.authorizer_approved.getStageId())
                    .orElseThrow(() -> new IllegalArgumentException("SOP Stage ID not found with ID: " + StageMasterEnum.authorizer_approved.getStageId()));

            User user = commonService.getUserDtls(authorisedBy);

            endorserRepo.updateApprovedStatus(authorizerStage.getId(), remark, sopFileId, user.getId());

            SopFileDetails sopFileDetails= sopFileRepo.findById(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopFileId));


            WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopFileId)
                    .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopFileId));


            BigInteger checkIfExist = sopFileRepo.checkIfExist(sopFileDetails.getSecID().getId());

            if (checkIfExist != null){

                List<ResponsibilityInterfaceDTO> rspList = responsibilityRepo.getRespIdList(checkIfExist.intValue());

                if (!CollectionUtils.isEmpty(rspList)){

                    for (ResponsibilityInterfaceDTO rspDTO: rspList){
                        responsibilityRepo.deleteDocMappingByRespId(rspDTO.getRespId());
                        responsibilityRepo.deleteSecMappingByRespId(rspDTO.getRespId());
                    }

                    sopFileRepo.deleteEndorserTasklistDtls(checkIfExist.intValue());
                    logRepo.deleteLog(checkIfExist.intValue());
                    sopFileRepo.deleteWkFlowDtls(checkIfExist.intValue());
                    sopFileRepo.deleteRespDtls(checkIfExist.intValue());
                    sopFileRepo.deleteById(checkIfExist.intValue());
                }
            }

            List<ResponsibilityInterfaceDTO> responsilityList = responsibilityRepo.getRespIdList(sopFileId);
            SopTypeMaster sopTypeMaster = sopTypeMasterRepo.findById(1)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Type ID not found with ID: " + 1));
            if (!CollectionUtils.isEmpty(responsilityList)){
                for (ResponsibilityInterfaceDTO responsibilityDTO:responsilityList){
                    Responsibilities responsibilities = responsibilityRepo.findById(responsibilityDTO.getRespId())
                            .orElseThrow(() -> new IllegalArgumentException("Responsibility details not found with ID: " + responsibilityDTO.getRespId()));

                    if (responsibilities.getTypeId().getId() == 2){
                        if ((responsibilities.getIsAddendum().charValue() =='Y') && (responsibilities.getWantToDelete().charValue() =='Y')){

                            String status = deleteObsoleteResponsibility(responsibilityDTO.getRespId(), user);

                            if (status.equalsIgnoreCase("Success")){
                                continue;
                            }

                        }else if(responsibilities.getIsAddendum().charValue() =='Y') {
                            responsibilities.setEffectiveDate(LocalDateTime.now());
                            responsibilities.setIsEndorsed('Y');
                            responsibilities.setIsAddendum('N');
                            responsibilities.setTypeId(sopTypeMaster);
                            responsibilityRepo.save(responsibilities);
                        }

                    }else {
                        if (responsibilities.getIsEndorsed().charValue() =='N'){
                            responsibilities.setEffectiveDate(LocalDateTime.now());
                            responsibilities.setIsEndorsed('Y');
                            responsibilities.setTypeId(sopTypeMaster);
                            responsibilityRepo.save(responsibilities);
                        }
                    }

                }
            }
            commonService.getWorkFlowDtls(remark, authorizerStage, user, sopFileDetails, workFlowDetails, workFLowRepo, logRepo);

            var logEntity = SopLogDetails.builder()
                    .actionTime(LocalDateTime.now())
                    .actionTakenBy(user)
                    .sopFileId(sopFileDetails)
                    .sopVersion(sopFileDetails.getSopVersion())
                    .remarks(remark)
                    .action("Approved by Authorizer")
                    .build();
            logRepo.save(logEntity);


            if (sopFileDetails.getSopTypeMaster().getId() == 2){
                sopFileDetails.setSopTypeMaster(sopTypeMaster);
                sopFileRepo.save(sopFileDetails);
            }



            /*String fullName = sopFileDetails.getCreatedBy().getFirstName() +" "+sopFileDetails.getCreatedBy().getLastName();
            String gender = "";
            if (sopFileDetails.getCreatedBy().getGender().charValue()=='M'){
                gender = "Mr";
            }else {
                gender = "Ms";
            }*/

            String mailContent =
                    "<html> <body>"+"Dear Sir/Madam,<br>"
                            + "The Proposed SOP of your section has been endorsed by the Managing Director. <br>"
                            + "<p>Thank You</p>"
                            +"</body></html>";

            MailSender.sendMail(sopFileDetails.getCreatedBy().getEmailId(), null, null, mailContent, "SOP Endorsed By Managing Director");

            List<HistoryDetailsEntity> changeHistoryDtls = changeHistoryRepo.findChangeHistory(sopFileId, sopFileDetails.getSopTypeMaster().getId());

            if (!CollectionUtils.isEmpty(changeHistoryDtls)){

                for(HistoryDetailsEntity historyDetails : changeHistoryDtls){

                    if(historyDetails.getSopType().getId() == 2 && historyDetails.getIsAddendum().charValue() == 'Y'){
                        historyDetails.setCurrentSopNo(sopFileDetails.getSopVersion());
                        historyDetails.setEffectiveDate(LocalDateTime.now());
                        historyDetails.setIsEndorsed('Y');
                    }else {
                        historyDetails.setCurrentSopNo(sopFileDetails.getSopVersion());
                        historyDetails.setEffectiveDate(LocalDateTime.now());
                        historyDetails.setIsEndorsed('Y');
                    }
                    changeHistoryDtls.add(historyDetails);
                }
                changeHistoryRepo.saveAll(changeHistoryDtls);
            }

            sopResponse.setResponseText("Approved by Authorizer");
            sopResponse.setSuccess(true);
            sopResponse.setResponseCode(1);

        }catch (Exception e){
            sopResponse.setResponseText(e.getMessage());
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(sopResponse);
        }

        return ResponseEntity.ok().body(sopResponse);
    }

    private String deleteObsoleteResponsibility(Integer respId, User authorisedBy) {

        inter_resp_mapping_repo.deleteInterRelatedRespTo(respId);
        inter_resp_mapping_repo.deleteInterRelatedRespFrm(respId);
        responsibilityRepo.deleteSecMappingByRespId(respId);
        responsibilityRepo.deleteDocMappingByRespId(respId);
        responsibilityRepo.deleteById(respId);

        return "Success";
    }

    @Override
    public byte[] saveSOP(ViewSOPResponsibilityInterface viewSOPTitle, List<HistoryDetailsDTO> changeHistory, Integer sopId, HttpServletRequest request, List<ResponsibilityDTO> responsibilityListBySopId) {
        ViewSopDetailsInterfaceDTO description = sopFileRepo.getSopDesc(sopId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream))) {
            try (com.itextpdf.layout.Document document = new Document(pdfDoc)) {

                // Create a Thymeleaf context and set the model attributes
                Context context = new Context();
                context.setVariable("sopData", viewSOPTitle);
                context.setVariable("changeHistory", changeHistory);
                context.setVariable("description", description);
                context.setVariable("responsibilities", responsibilityListBySopId);

                List<ResponsibilityDTO> responsibilityDTOList = new ArrayList<>();
                for(ResponsibilityDTO res : responsibilityListBySopId) {

                    ResponseEntity<ResponsibilityDTO> responseEntity = this.viewerService.getResponsibilityDtlsByViewer(res.getRespId());
                    ResponsibilityDTO responsibility = responseEntity.getBody();

                    String baseUrl = request.getRequestURL().toString();
                    String newBaseUrl = baseUrl.replaceFirst("/endorser/approveByAuthorizer", "");

                    List<SOPAttachmentDetailsDTO> attachmentList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(responsibility.getDocLst())) {
                        for (SOPAttachmentDetailsDTO docDTO : responsibility.getDocLst()) {
                            if(docDTO != null){
                                // Construct the full URL path by appending the UUID
                                String uuid = docDTO.getDocUUID();
                                String fullPath = newBaseUrl + "/author/view/DownloadDocByAuthor/" + uuid;

                                SOPAttachmentDetailsDTO attachment = new SOPAttachmentDetailsDTO(fullPath, docDTO.getDocName());
                                attachmentList.add(attachment);
                            }else {
                                // Construct the full URL path by appending the UUID
                                SOPAttachmentDetailsDTO attachment = new SOPAttachmentDetailsDTO(null, null);
                                attachmentList.add(attachment);
                            }
                        }

                    } else {
                        attachmentList.add(null);
                    }
                    responsibility.setDocLst(attachmentList);

                    responsibilityDTOList.add(responsibility);
                }
                context.setVariable("procedure", responsibilityDTOList);

                ResourceBundle resource = ResourceBundle.getBundle("documentUploads");
                String filePathPrefix = resource.getString("sopCover");
                context.setVariable("coverPhoto", filePathPrefix);

                // Process the Thymeleaf template and add it to the PDF

                String htmlContent = templateEngine.process("template", context);
                HtmlConverter.convertToPdf(htmlContent, pdfDoc, new ConverterProperties());

                System.out.println(htmlContent);

            }
        }
        return outputStream.toByteArray();
    }

    @Override
    public void saveEndorsedSOP(String name, Integer sopId, byte[] pdfBytes) {
        //Save in DataBase

        SopFileDetails sopFileDetails= sopFileRepo.findById(sopId)
                .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopId));

        String randomUUID = UUID.randomUUID().toString();

        ResourceBundle resourceBundle = ResourceBundle.getBundle("documentUploads");
        String filePathPrefix = resourceBundle.getString("endorsedSop");
        String filePath = filePathPrefix+"/"+name;

        try {
            FileUtils.writeByteArrayToFile(new File(filePath), pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var saveSop = EndorsedSOPDoc.builder()
                .sopId(sopFileDetails)
                .document_name(name)
                .filePath(filePath)
                .uuid(randomUUID)
                .build();
        endorsedSopDocRepo.save(saveSop);
    }

}
