package com.tashicell.sop.Controller;

import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.EndorsedSopDocRepo;
import com.tashicell.sop.Repository.SopFileRepo;
import com.tashicell.sop.Service.ViewerService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/viewer")
public class ViewerController {
    private final ViewerService viewerService;

    private final SopFileRepo sopFileRepo;

    private final EndorsedSopDocRepo endorsedSopRepo;

    @GetMapping("/viewerTaskList/{userId}")
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> viewerTaskList(@PathVariable("userId") String userId) {

        return this.viewerService.getViewerTaskList(userId);
    }

    @GetMapping("/getOtherDepartmentSOP/{deptId}")
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getOtherDepartmentSOP(@PathVariable("deptId") Integer deptId) {

        return this.viewerService.getOtherDepartmentSOP(deptId);
    }

    @GetMapping("/getSopTitleDetails/{sopId}")
    public ResponseEntity<ViewSOPResponsibilityInterface> getSopTitleDetails(@PathVariable("sopId") Integer sopId) {

        return this.viewerService.getSopTitleDetails(sopId);
    }
    @GetMapping("/getChangeHistory/{sopId}/{userId}")
    public ResponseEntity<List<HistoryDetailsDTO>> getChangeHistory(@PathVariable("sopId") Integer sopId, @PathVariable("userId") String userId) {

        return this.viewerService.getChangeHistory(sopId, userId);
    }

    @GetMapping("/getSopDescription/{sopId}")
    public ResponseEntity<ViewSopDetailsInterfaceDTO> getSopDescription(@PathVariable("sopId") Integer sopId) {

        return this.viewerService.getSopDescription(sopId);
    }
    @GetMapping("/getResponsibilityDetails/{sopId}")
    public ResponseEntity<List<ResponsibilityDTO>> getResponsibilityDetailsByViewer(@PathVariable("sopId") Integer sopId) {

        return this.viewerService.getResponsibilityListBySopId(sopId);
    }

    @GetMapping("/getResponsibilityDtlsByViewer/{respId}")
    public ResponseEntity<ResponsibilityDTO> getResponsibilityDtlsByViewer(@PathVariable("respId") Integer respId) {

        return this.viewerService.getResponsibilityDtlsByViewer(respId);
    }

    @GetMapping("/getRelatedActivities/{userId}")
    public ResponseEntity<List<RelatedDeptSOPInterface>> getRelatedActivities(@PathVariable("userId") String empId) {

        return this.viewerService.getRelatedActivities(empId);
    }

    @GetMapping("/getOtherRelatedActivities/{sopId}")
    public ResponseEntity<List<RelatedDeptSOPInterface>> getOtherRelatedActivities(@PathVariable("sopId") Integer sopId) {

        return this.viewerService.getOtherRelatedActivities(sopId);
    }

    @GetMapping("/getViewerActivityLog/{sopId}/{sopVersion}")
    public ResponseEntity<List<ActivityLogInterfaceDTO>> getViewerActivityLog(@PathVariable("sopId") Integer sopId, @PathVariable("sopVersion") String sopVersion){
        return ResponseEntity.ok(this.viewerService.getViewerActivityLog(sopId, sopVersion));
    }

    @GetMapping(value = "/generatePDF/{sopId}/{userId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> generatePdf(@PathVariable("sopId") Integer sopId, @PathVariable("userId") String userId, HttpServletRequest request) throws IOException {

        ResponseEntity<ViewSOPResponsibilityInterface> viewSOPTitle = this.viewerService.getSopTitleDetails(sopId);

        ViewSOPResponsibilityInterface sopTitle = viewSOPTitle.getBody();

        ResponseEntity<List<HistoryDetailsDTO>> history = this.viewerService.getChangeHistory(sopId, userId);

        List<HistoryDetailsDTO> changeHistory = history.getBody();

        ResponseEntity<List<ResponsibilityDTO>> responsibilityListBySopId = this.viewerService.getResponsibilityListBySopId(sopId);

        List<ResponsibilityDTO> responsibilityList = responsibilityListBySopId.getBody();

        // Generate PDF using data from the database
        byte[] pdfBytes = this.viewerService.generatePdf(sopTitle, changeHistory, sopId, request, responsibilityList);

        // Create a ByteArrayResource from the PDF bytes
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+sopTitle.getSopTitle()+"_SOP.pdf");


        // Return the PDF as a ResponseEntity
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/getHyperlinkRespContent/{respId}")
    public ResponseEntity<ResponsibilityDTO> getHyperlinkRespContentFrom(@PathVariable("respId") Integer respId) {
        return ResponseEntity.ok(this.viewerService.getHyperlinkRespContent(respId));
    }

}
