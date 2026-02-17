package com.tashicell.sop.Service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.tashicell.sop.Modal.Responsibilities;
import com.tashicell.sop.Modal.SopFileDetails;
import com.tashicell.sop.Modal.User;
import com.tashicell.sop.Modal.WorkFlowDetails;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ViewerServiceImpl implements ViewerService {

    private final ResponsibilityRepo responsibilityRepo;

    private final CommonServiceImpl commonService;

    private final SopFileRepo sopFileRepo;

    private final ChangeHistoryRepo changeHistoryRepo;

    private final DocuRepo docuRepo;

    private final LogRepo logRepo;

    private final TemplateEngine textTemplateEngine;

    private final SopWorkFLowRepo workFLowRepo;

    private final UserRepo userRepo;
    @Override
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getViewerTaskList(String userId) {
        try {

            User userDetails = commonService.getUserDtls(userId);

            List<ViewSopDetailsInterfaceDTO> viewSopDetailsInterfaceDTOList = sopFileRepo.getViewerTaskList(userDetails.getDepartmentMaster().getId());

            return ResponseEntity.ok().body(viewSopDetailsInterfaceDTOList);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @Override
    public ResponseEntity<List<HistoryDetailsDTO>> getChangeHistory(Integer sopId, String userId) {

        List<HistoryDetailsDTO> historyDetailsList = new ArrayList<>();

        User user= userRepo.findUserID(userId)
                .orElseThrow(() -> new IllegalArgumentException("User Details not found with ID: " + userId));

        try {

            String sopVersion = "";

            if (user.getRole().getId() == 2){

                SopFileDetails sopFileDetails = sopFileRepo.findById(sopId)
                        .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopId));

                if (sopFileDetails.getSopTypeMaster().getId() == 2){
                    if (user.getSectionMaster().getId() == sopFileDetails.getSecID().getId()){
                        sopVersion = "N";
                    }else {
                        sopVersion = "Y";
                    }
                }else {
                    sopVersion = "Y";
                }
            }
            else {
                sopVersion = "Y";
            }
            List<HistoryDetailsInterface> historyDetailsInterfaceList = changeHistoryRepo.getChangeHistory(sopId, sopVersion);

            for(int i = 0; i < historyDetailsInterfaceList.size(); i ++){
                HistoryDetailsDTO historyDetailsDTO = new HistoryDetailsDTO();
                historyDetailsDTO.setSignificantChange(historyDetailsInterfaceList.get(i).getSignificantChange());
                historyDetailsDTO.setEffectiveDate(historyDetailsInterfaceList.get(i).getEffectiveDate());
                historyDetailsDTO.setPreviousSopNo(historyDetailsInterfaceList.get(i).getPreviousSopNo());
                historyDetailsDTO.setIsAddendum(historyDetailsInterfaceList.get(i).getIsAddendum());

                if (historyDetailsInterfaceList.get(i).getSopTypeId() == 1){
                    historyDetailsDTO.setSopNo("V1.0");
                }
                else {
                    historyDetailsDTO.setSopNo("Addendum "+historyDetailsInterfaceList.get(i).getCurrentSopVno());
                }


                historyDetailsList.add(historyDetailsDTO);

            }
            return ResponseEntity.ok().body(historyDetailsList);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<ViewSOPResponsibilityInterface> getSopTitleDetails(Integer sopId) {

        try {

            WorkFlowDetails workFlowDetails = workFLowRepo.getWorkFlowDtls(sopId)
                    .orElseThrow(() -> new IllegalArgumentException("Work Flow details not found with ID: " + sopId));

            if(workFlowDetails.getSopVersion().equalsIgnoreCase("V1.0")){

                ViewSOPResponsibilityInterface viewSOPResponsibilityInterface = responsibilityRepo.getSopTitleDetails(sopId);
                return ResponseEntity.ok().body(viewSOPResponsibilityInterface);
            }else{
                ViewSOPResponsibilityInterface viewSOPResponsibilityInterface = responsibilityRepo.getSopTitleDetailSFromAudit(sopId);
                return ResponseEntity.ok().body(viewSOPResponsibilityInterface);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<ViewSopDetailsInterfaceDTO> getSopDescription(Integer sopId) {
        ViewSopDetailsInterfaceDTO getDescription = sopFileRepo.getSopDesc(sopId);

        return ResponseEntity.ok().body(getDescription);

    }

    @Override
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getOtherDepartmentSOP(Integer deptId) {
        try {

            List<ViewSopDetailsInterfaceDTO> viewSopDetailsInterfaceDTOList = sopFileRepo.getViewerTaskList(deptId);

            return ResponseEntity.ok().body(viewSopDetailsInterfaceDTOList);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<List<ResponsibilityDTO>> getResponsibilityListBySopId(Integer sopId) {
        List<ResponsibilityDTO> responsibilityDTOList = new ArrayList<>();

        List<ResponsibilityInterfaceDTO> responsibilityInterfaceList = new ArrayList<>();

        List<ResponsibilityInterfaceDTO> rspList = responsibilityRepo.getEndorsedRespList(sopId);

        if (!CollectionUtils.isEmpty(rspList)){
            for(ResponsibilityInterfaceDTO responsibilityLst : rspList){
                Responsibilities responsibilities = responsibilityRepo.findById(responsibilityLst.getRespId())
                        .orElseThrow(() -> new IllegalArgumentException(""));

                if (responsibilities.getIsEndorsed().charValue() == 'Y') {
                    responsibilityInterfaceList = responsibilityRepo.viewResponsibilitiesLstByViewer(responsibilityLst.getRespId());

                }else {
                    if (responsibilities.getTypeId().getId() ==2){
                        responsibilityInterfaceList = responsibilityRepo.getOngoingAddendumResp(responsibilityLst.getRespId());
                    }
                }

                if (!CollectionUtils.isEmpty(responsibilityInterfaceList)) {
                    for (ResponsibilityInterfaceDTO responsibilityInterfaceDTO : responsibilityInterfaceList) {


                        ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO(responsibilityInterfaceDTO.getRespId(), responsibilityInterfaceDTO.getResponsibilityName(), null,
                                responsibilityInterfaceDTO.getRoleHolderName(), null, responsibilityInterfaceDTO.getSecList(), null,
                                null, null, null, null, responsibilityInterfaceDTO.getDeptName(), responsibilityInterfaceDTO.getEffectiveDate(),
                                responsibilityInterfaceDTO.getDepartmentName(), null, null, null, null,
                                responsibilityInterfaceDTO.getRelatedStatus(), responsibilityInterfaceDTO.getRelatedRemarks(), null, null);

                        responsibilityDTOList.add(responsibilityDTO);
                    }

                }
            }
        }

        return ResponseEntity.ok().body(responsibilityDTOList);
    }

    @Override
    public ResponseEntity<ResponsibilityDTO> getResponsibilityDtlsByViewer(Integer respId) {

        try {
            List<ResponsibilityInterfaceDTO> responsibilityInterfaceDTOList = new ArrayList<>();

            Responsibilities responsibilities = responsibilityRepo.findById(respId)
                    .orElseThrow(() -> new IllegalArgumentException("Responsibility details not found"));

            if (responsibilities.getIsEndorsed().charValue() == 'Y') {
                if (responsibilities.getIsAddendum().charValue() == 'Y'){
                    responsibilityInterfaceDTOList = responsibilityRepo.viewOldResponsibilitiesLstDtlsViewer(respId);
                }else {
                    responsibilityInterfaceDTOList = responsibilityRepo.viewResponsibilitiesLstDtlsViewer(respId);

                }
            }else {
                if (responsibilities.getIsAddendum().charValue() == 'Y'){
                    responsibilityInterfaceDTOList = responsibilityRepo.viewOldResponsibilitiesLstDtlsViewer(respId);
                }

            }

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

                    responsibilityDTO.setRespId(responsibilityInterfaceDTO.getRespId());
                    responsibilityDTO.setResponsibilityName(responsibilityInterfaceDTO.getResponsibilityName());
                    responsibilityDTO.setRemarks(responsibilityInterfaceDTO.getRemarks());
                    responsibilityDTO.setSectionList(responsibilityInterfaceDTO.getSecList());
                    responsibilityDTO.setContent(responsibilityInterfaceDTO.getContent());
                    responsibilityDTO.setDeptName(responsibilityInterfaceDTO.getDeptName());
                    responsibilityDTO.setDocLst(attachmentList);

                    if (responsibilityInterfaceDTO.getRespIdFrom() != null){
                        if (responsibilityInterfaceDTO.getRespId().intValue() == responsibilityInterfaceDTO.getRespIdFrom().intValue()){
                            if(responsibilityInterfaceDTO.getRespIdTo() != null){
                                Responsibilities getRespIdFrom = responsibilityRepo.findById(responsibilityInterfaceDTO.getRespIdTo())
                                        .orElseThrow(() -> new IllegalArgumentException("Responsibility details not found"));
                                if (getRespIdFrom.getIsEndorsed().charValue() == 'Y'){
                                    responsibilityDTO.setRespRelatedID(responsibilityInterfaceDTO.getRespIdTo());
                                }
                            }
                        }
                    }
                    if (responsibilityInterfaceDTO.getRespIdTo() != null){
                        if (responsibilityInterfaceDTO.getRespId().intValue() == responsibilityInterfaceDTO.getRespIdTo().intValue()){
                            if(responsibilityInterfaceDTO.getRespIdFrom() != null){
                                Responsibilities getRespIdTo = responsibilityRepo.findById(responsibilityInterfaceDTO.getRespIdFrom())
                                        .orElseThrow(() -> new IllegalArgumentException("Responsibility details not found"));
                                if (getRespIdTo.getIsEndorsed().charValue() == 'Y'){
                                    responsibilityDTO.setRespRelatedID(responsibilityInterfaceDTO.getRespIdFrom());
                                }
                            }

                        }
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
    public List<ActivityLogInterfaceDTO> getViewerActivityLog(Integer sopId, String sopVersion) {

        if(sopVersion.equalsIgnoreCase("NA")){
            sopVersion = "v1.0";
        }else {
            String previousVersion[] = sopVersion.split("\\.");
            String part1 = previousVersion[0];
            Integer part2 = Integer.valueOf(previousVersion[1]);
            sopVersion = part1+"."+(part2+1);
        }
        return this.logRepo.getViewerActivityLog(sopId, sopVersion);
    }

    @Override
    public byte[] generatePdf(ViewSOPResponsibilityInterface viewSOPTitle, List<HistoryDetailsDTO> changeHistory, Integer sopId, HttpServletRequest request, List<ResponsibilityDTO> responsibilityListBySopId) {
        ViewSopDetailsInterfaceDTO description = sopFileRepo.getSopDesc(sopId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream))) {
            try (Document document = new Document(pdfDoc, PageSize.A4)) {

                document.getPdfDocument();

                // Create a Thymeleaf context and set the model attributes
                Context context = new Context();
                context.setVariable("sopData", viewSOPTitle);
                context.setVariable("changeHistory", changeHistory);
                context.setVariable("description", description);
                context.setVariable("responsibilities", responsibilityListBySopId);

                List<ResponsibilityDTO> responsibilityDTOList = new ArrayList<>();
                for(ResponsibilityDTO res : responsibilityListBySopId) {

                    ResponseEntity<ResponsibilityDTO> responseEntity = getResponsibilityDtlsByViewer(res.getRespId());
                    ResponsibilityDTO responsibility = responseEntity.getBody();

                    String baseUrl = request.getRequestURL().toString();
                    String newBaseUrl = baseUrl.replaceFirst("/viewer/generatePDF/.*", "");

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

                String htmlContent = textTemplateEngine.process("template", context);

                HtmlConverter.convertToPdf(htmlContent, pdfDoc, new ConverterProperties());
            }
        }

        return outputStream.toByteArray();

}

    @Override
    public ResponsibilityDTO getHyperlinkRespContent(Integer respId) {
        Responsibilities responsibilities = getRespById(respId);

        ResponsibilityDTO responsibilityDTO = new ResponsibilityDTO();

        if (responsibilities.getIsEndorsed().charValue() == 'Y') {
            if (responsibilities.getIsAddendum().charValue() == 'Y'){
                ResponsibilityInterfaceDTO responsibilityInterfaceDTO = getInterRespContent(respId);

                responsibilityDTO.setRespId(responsibilityInterfaceDTO.getRespId());
                responsibilityDTO.setResponsibilityName(responsibilityInterfaceDTO.getResponsibilityName());
                responsibilityDTO.setContent(responsibilityInterfaceDTO.getContent());
            }else {
                responsibilityDTO.setRespId(responsibilities.getResId());
                responsibilityDTO.setResponsibilityName(responsibilities.getResponsibilityName());
                responsibilityDTO.setContent(responsibilities.getContent());

            }
        }else {
            if (responsibilities.getIsAddendum().charValue() == 'Y'){
                ResponsibilityInterfaceDTO responsibilityInterfaceDTO = getInterRespContent(respId);
                responsibilityDTO.setRespId(responsibilityInterfaceDTO.getRespId());
                responsibilityDTO.setResponsibilityName(responsibilityInterfaceDTO.getResponsibilityName());
                responsibilityDTO.setContent(responsibilityInterfaceDTO.getContent());
            }
        }

        return responsibilityDTO;
    }

    @Override
    public ResponseEntity<List<RelatedDeptSOPInterface>> getRelatedActivities(String empId) {
        try {
            User userDetails = commonService.getUserDtls(empId);
            List<RelatedDeptSOPInterface> viewRelatedResp = sopFileRepo.getRelatedActivities(userDetails.getSectionMaster().getId());

            return ResponseEntity.ok().body(viewRelatedResp);
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<List<RelatedDeptSOPInterface>> getOtherRelatedActivities(Integer sopId) {
        try {

            SopFileDetails sopFileDetails = sopFileRepo.findById(sopId)
                    .orElseThrow(() -> new IllegalArgumentException("SOP Details not found with ID: " + sopId));

            Integer secId = sopFileDetails.getSecID().getId();

            List<RelatedDeptSOPInterface> viewRelatedResp = sopFileRepo.getRelatedActivities(secId);

            return ResponseEntity.ok().body(viewRelatedResp);
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private ResponsibilityInterfaceDTO getInterRespContent(Integer respId) {
        ResponsibilityInterfaceDTO responsibilityInterfaceDTO = responsibilityRepo.viewOldResponsibilityContent(respId);
        return responsibilityInterfaceDTO;
    }

    private Responsibilities getRespById(Integer respId) {
        Responsibilities respEntityId = responsibilityRepo.findById(respId)
                .orElseThrow(() -> new IllegalArgumentException("Responsibility Details not found with ID: " +respId));
        return respEntityId;
    }
}
