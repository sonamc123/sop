package com.tashicell.sop.Controller;

import com.tashicell.sop.Record.*;
import com.tashicell.sop.Service.ReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviewer")
public class ReviewerController {

    private final ReviewerService reviewerService;

    //View by Reviewer
    @GetMapping("/getReviewerTaskList/{userId}")
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getReviewerTaskList(@PathVariable("userId") String empId) {

        return this.reviewerService.getReviewerTaskList(empId);
    }

    @GetMapping("/reviewByReviewer/{sopFileId}")
    public ResponseEntity<List<ResponsibilityDTO>> reviewByReviewer(@PathVariable("sopFileId") Integer sopFileId) {

        return this.reviewerService.getResponsibilityListByReviewer(sopFileId);
    }

    //Approve By Reviewer
    @PutMapping("/approveByReviewer")
    public ResponseEntity<SopResponse> approveByReviewer(@RequestParam("sopFileId") Integer sopFileId, @RequestParam("reviewedBy") String reviewedBy, @RequestParam("remark") String remark) {

        return this.reviewerService.approveByReviewer(sopFileId, reviewedBy, remark);
    }

    //Reject By Reviewer
    @PutMapping("/rejectByReviewer")
    public ResponseEntity<SopResponse> rejectedByReview(@RequestParam("sopFileId") Integer sopFileId, @RequestParam("rejectedBy") String reviewedBy, @RequestParam("remark") String remark) {

        return this.reviewerService.rejectedByReview(sopFileId, reviewedBy, remark);
    }

    //Update Responsibility Content By Reviewer/Focal Person
    @PutMapping("/updateContentByReviewer")
    public ResponseEntity<SopResponse> updateContentByReviewer(@RequestPart("data") ResponsibilityDTO responsibilityDTO) {

        Integer respId = responsibilityDTO.getRespId();
        String respContent = responsibilityDTO.getContent();
        String updatedBy = responsibilityDTO.getUpdatedBy();
        return this.reviewerService.updateContentByReviewer(respId, respContent, updatedBy);
    }
}
