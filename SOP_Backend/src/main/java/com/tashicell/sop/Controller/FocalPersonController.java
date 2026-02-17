package com.tashicell.sop.Controller;

import com.tashicell.sop.Record.DocumentInterfaceViewDTO;
import com.tashicell.sop.Record.SopResponse;
import com.tashicell.sop.Record.ViewSopDetailsInterfaceDTO;
import com.tashicell.sop.Record.ViewStatusInterface;
import com.tashicell.sop.Service.FocalPersonService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/focalPerson")
public class FocalPersonController {

    private final FocalPersonService focalPersonService;

    @GetMapping("/getFocalTaskList")
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getFocalTaskList() {

        return this.focalPersonService.getFocalTaskList();
    }

    @PutMapping("/submitToReviewer")
    public ResponseEntity<SopResponse> submitForReview(@RequestParam("sopFileId") Integer sopFileId, @RequestParam("updatedBy") String updatedBy, @RequestParam("remark") String remark) {

        return this.focalPersonService.submitForReview(sopFileId, updatedBy, remark);
    }
    @PutMapping("/rejectByFocal")
    public ResponseEntity<SopResponse> rejectByFocal(@RequestParam("sopFileId") Integer sopFileId, @RequestParam("rejectedBy") String reviewedBy, @RequestParam("remark") String remark) {

        return this.focalPersonService.rejectedByFocalPerson(sopFileId, reviewedBy, remark);
    }

    @GetMapping("/getSopHistoryByFocal/{dept_Id}")
    public ResponseEntity<List<DocumentInterfaceViewDTO>> getSopHistoryByFocal(@PathVariable("dept_Id") Integer dept_Id) {

        return this.focalPersonService.getSopHistoryByFocal(dept_Id);
    }

    @GetMapping("/getAllStatusDetails")
    public List<ViewStatusInterface> getAllStatusDetails() {

        return this.focalPersonService.getAllStatusDetails();
    }

    @GetMapping("/getViewStatus/{stageID}/{deptId}")
    public List<ViewStatusInterface> getViewStatus(@PathVariable("stageID") Integer stageID, @PathVariable("deptId") Integer deptId) {

        return this.focalPersonService.getViewStatus(stageID, deptId);
    }

    @GetMapping("/getEndorserPendingList/{sopId}")
    public List<ViewStatusInterface> getEndorserPendingList(@PathVariable("sopId") Integer sopId) {

        return this.focalPersonService.getEndorserPendingList(sopId);
    }


    @GetMapping("/downloadEndorsedSop/{docUUID}")
    public String viewDocByAuthor(@PathVariable("docUUID") String docUUID, HttpServletResponse response) {

        try {
            DocumentInterfaceViewDTO doc = focalPersonService.downloadEndorsedSop(docUUID);
            byte[] fileContent = downloadFile(doc.getUpload_URL());

            if (doc.getDocName().substring(doc.getDocName().length() - 3).equalsIgnoreCase("JPG") || doc.getDocName().substring(doc.getDocName().length() - 4).equalsIgnoreCase("jpeg") || doc.getDocUUID().substring(doc.getDocName().length() - 3).equalsIgnoreCase("png")) {
                response.setContentType("image/jpeg");
                response.setHeader("Content-disposition", "inline; filename=" + doc.getDocName());
                response.getOutputStream().write(fileContent);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            } else if(doc.getDocName().substring(doc.getDocName().length()-3).equalsIgnoreCase("pdf")){
                response.setContentType("APPLICATION/PDF");
                response.setHeader("Content-disposition", "inline; filename="+doc.getDocName());
                response.getOutputStream().write(fileContent);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
            else if(doc.getDocName().substring(doc.getDocName().length()-4).equalsIgnoreCase("docx")){
                response.reset();
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                response.setHeader("Content-Disposition", "inline;filename="+doc.getDocName());
                response.getOutputStream().write(fileContent);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
            else if(doc.getDocName().substring(doc.getDocName().length()-3).equalsIgnoreCase("xls")){
                response.setContentType("APPLICATION/vnd.ms-excel");
                response.setHeader("Content-disposition", "inline; filename="+doc.getDocName());
                response.getOutputStream().write(fileContent);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
            else if(doc.getDocName().substring(doc.getDocName().length()-4).equalsIgnoreCase("xlsx")){
                response.setContentType("Application/x-msexcel");
                response.setHeader("Content-disposition", "inline; filename="+doc.getDocName());
                response.getOutputStream().write(fileContent);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
            else{
                response.setContentType("application/octet-stream");
                response.setHeader("Content-disposition", "attachment; filename="+doc.getDocName());
                response.getOutputStream().write(fileContent);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }

        } catch (Exception e) {
            System.out.print(e);
            return "" + e;
        }
        return null;
    }

    public static byte[] downloadFile(String uploadUlr) throws Exception{
        FileInputStream fileInputStream = new FileInputStream(uploadUlr);
        return IOUtils.toByteArray(fileInputStream);
    }

}
