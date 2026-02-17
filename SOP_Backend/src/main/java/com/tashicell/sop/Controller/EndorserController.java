package com.tashicell.sop.Controller;

import com.tashicell.sop.Record.*;
import com.tashicell.sop.Service.EndorserService;
import com.tashicell.sop.Service.ReviewerServiceImpl;
import com.tashicell.sop.Service.ViewerServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/endorser")
public class  EndorserController {

    private final EndorserService endorserService;
    private final ReviewerServiceImpl reviewerService;

    private final ViewerServiceImpl viewerService;

    //View by Endorser
    @GetMapping("/getEndorserTaskList/{userId}")
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getEndorserTaskList(@PathVariable("userId") String empId) {

        return this.endorserService.getEndorserTaskList(empId);
    }

    //View by Authorizer, MD Sir
    @GetMapping("/getAuthorizerTaskList")
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getAuthorizerTaskList() {

        return this.endorserService.getAuthorizerTaskList();
    }

    @GetMapping("/viewByEndorser/{sopFileId}")
    public ResponseEntity<List<ResponsibilityDTO>> viewByEndorser(@PathVariable("sopFileId") Integer sopFileId) {
        return this.reviewerService.getResponsibilityListByReviewer(sopFileId);
    }

    @GetMapping("/getEndorserRemark/{sopFileId}")
    public ResponseEntity<List<EndorserRemarkInterface>> getEndorserRemark(@PathVariable("sopFileId") Integer sopFileId) {
        return this.endorserService.getEndorserRemark(sopFileId);
    }

    //Approve By Endorser
    @PutMapping("/approveByEndorser")
    public ResponseEntity<SopResponse> approveByEndorser(@RequestParam("sopFileId") Integer sopFileId, @RequestParam("approvedBy") String approvedBy, @RequestParam("remark") String remark) {
        return this.endorserService.approveByEndorser(sopFileId, approvedBy, remark);
    }

    //Reject By Endorser/Authorizer
    @PutMapping("/rejectByEndorser")
    public ResponseEntity<SopResponse> rejectByEndorser(@RequestParam("sopFileId") Integer sopFileId, @RequestParam("rejectedBy") String updatedBy, @RequestParam("remark") String remark) {
        return this.endorserService.rejectByEndorser(sopFileId, updatedBy, remark);
    }

    //Approve By Authorizer, MD Sir
    @PutMapping("/approveByAuthorizer")
    public ResponseEntity<SopResponse> approveByAuthorizer(@RequestParam("sopFileId") Integer sopId, @RequestParam("approvedBy") String approvedBy, @RequestParam("remark") String remark, HttpServletRequest request) throws IOException {

        ResponseEntity<SopResponse> responseEntity = endorserService.approveByAuthorizer(sopId, approvedBy, remark);

        ResponseEntity<ViewSOPResponsibilityInterface> viewSOPTitle = this.viewerService.getSopTitleDetails(sopId);
        ViewSOPResponsibilityInterface sopTitle = viewSOPTitle.getBody();

        ResponseEntity<List<HistoryDetailsDTO>> history = this.viewerService.getChangeHistory(sopId, approvedBy);

        List<HistoryDetailsDTO> changeHistory = history.getBody();

        ResponseEntity<List<ResponsibilityDTO>> responsibilityListBySopId = this.viewerService.getResponsibilityListBySopId(sopId);

        List<ResponsibilityDTO> responsibilityList = responsibilityListBySopId.getBody();

        // Generate PDF using data from the database
        byte[] pdfBytes = this.endorserService.saveSOP(sopTitle, changeHistory, sopId, request, responsibilityList);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+sopTitle.getSopTitle()+"_"+sopTitle.getSopNumber()+"_SOP.pdf");

        String SopName = sopTitle.getSopTitle()+"_"+sopTitle.getSopNumber()+"_SOP.pdf";

        endorserService.saveEndorsedSOP(SopName, sopId, pdfBytes) ;

        return responseEntity;
    }
}
