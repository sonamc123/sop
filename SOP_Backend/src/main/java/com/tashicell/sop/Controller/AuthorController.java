package com.tashicell.sop.Controller;

import com.tashicell.sop.Record.*;
import com.tashicell.sop.Service.AuthorService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/author")
public class AuthorController {

    private final AuthorService authorService;

    //Create by Author
    @PostMapping("/createNewSOP")
    public ResponseEntity<SopResponse> createSop(@RequestPart("data") RequestNewEndorsementDTO createSOPDTO, @RequestPart("File") MultipartFile[] file) {
        return this.authorService.createNewSop(createSOPDTO, file);
    }
    
    //View My File details
    @GetMapping("/viewMyFile/{userId}")
    public ResponseEntity<List<SOPContentDetailsDTO>> viewMyFileContent(@PathVariable("userId") String empId) {
        return this.authorService.viewMyFileContent(empId);
    }

    @GetMapping("/getResponsibilityList/{sopId}")
    public ResponseEntity<List<ResponsibilityDTO>> getResponsibilityListBySopId(@PathVariable("sopId") Integer sopId) {

        return this.authorService.getResponsibilityListBySopId(sopId);
    }

    @GetMapping("/getAddendumDetails/{sopId}")
    public ResponseEntity<AddendumRemarks> getAddendumDetails(@PathVariable("sopId") Integer sopId) {

        return this.authorService.getAddendumDetails(sopId);
    }

    @GetMapping("/getResponsibilityDtlsByRespId/{respId}")
    public ResponseEntity<ResponsibilityDTO> getResponsibilityDtlsByRespId(@PathVariable("respId") Integer respId) {

        return this.authorService.getResponsibilityDtlsByRespId(respId);
    }
    @GetMapping("/getRelatedActivityDetails/{respId}/{user}")
    public ResponseEntity<ResponsibilityDTO> getRelatedActivityDetails(@PathVariable("respId") Integer respId, @PathVariable("user") String user) {

        return this.authorService.getRelatedActivityDetails(respId, user);
    }

    //Edit My File details
    @GetMapping("/editMyFile/{sopID}")
    public ResponseEntity<List<ResponsibilityDTO>> editMyFile(@PathVariable("sopID") Integer sopID) {

        return this.authorService.editMyFile(sopID);
    }

    @PostMapping("/addResponsibility")
    public ResponseEntity<SopResponse> addResponsibility(@RequestPart("data") RequestNewEndorsementDTO addResponsibilityDTO, @RequestPart("File") MultipartFile[] addFile){

        return this.authorService.addResponsibility(addResponsibilityDTO,addFile);
    }

    @PutMapping("/updateIntro")
    public ResponseEntity<SopResponse> updateIntro(@RequestParam("sopId") Integer sopId, @RequestParam("introduction") String introduction, @RequestParam("updatedBy") String updatedBy){

        return this.authorService.updateIntro(sopId, introduction, updatedBy);
    }

    @PutMapping("/updateResponsibility")
    public ResponseEntity<SopResponse> updateResponsibility(@RequestPart("data") ResponsibilityDTO updateResponsibilityDTO, @RequestPart("File") MultipartFile[] updateFile){

        return this.authorService.updateResponsibility(updateResponsibilityDTO, updateFile);
    }
    @PutMapping("/deleteResponsibility")
    public ResponseEntity<SopResponse> deleteResponsibilityFromMyFile(@RequestParam("respId") Integer respId, @RequestParam("actionBy") String actionBy){
    	return this.authorService.deleteResponsibility(respId, actionBy);
    }

    @PutMapping("/deleteObsoleteResponsibility")
    public ResponseEntity<SopResponse> deleteResponsibilityFromEndorsement(@RequestParam("respId") Integer respId, @RequestParam("actionBy") String actionBy, @RequestParam("remarks") String remarks){
        return this.authorService.deleteResponsibilityFromEndorsement(respId, actionBy, remarks);
    }

    @PutMapping("/addendum")
    public ResponseEntity<List<ResponsibilityDTO>> addendum(@RequestParam("sopId") Integer sopId, @RequestParam("sopTypeId") Integer sopTypeId, @RequestParam("introduction") String introduction, @RequestParam("reason") String reason, @RequestParam("updatedBy") String updatedBy){

        return this.authorService.addendum(sopId, sopTypeId, introduction, reason, updatedBy);
    }

    @PostMapping("/submitToFocalPerson")
    public ResponseEntity<SopResponse> submitToFocalPerson(@RequestParam("sopFileId") Integer sopFileId, @RequestParam("updatedBy") String updatedBy, @RequestParam("remark") String remark) {

        return this.authorService.submitToFocalPerson(sopFileId, updatedBy, remark);
    }

    @GetMapping("/getEndorsedSOP/{userId}")
    public ResponseEntity<ViewSopDetailsInterfaceDTO> getEndorsedSOP(@PathVariable("userId") String empId) {

        return this.authorService.getEndorsedSOP(empId);
    }

    @GetMapping("/getRelatedResponsibility/{userId}")
    public ResponseEntity<List<RelatedDeptSOPInterface>> getRelatedResponsibility(@PathVariable("userId") String empId) {

        return this.authorService.getRelatedResponsibility(empId);
    }

    @GetMapping("/view/DownloadDocByAuthor/{docUUID}")
    public String viewDocByAuthor(@PathVariable("docUUID") String docUUID, HttpServletResponse response) {

        try {
            DocumentInterfaceViewDTO doc = authorService.DownloadDocByAuthor(docUUID);
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
            return "" + e;
        }
        return null;
    }

    public static byte[] downloadFile(String uploadUlr) throws Exception{
        FileInputStream fileInputStream = new FileInputStream(uploadUlr);
        return IOUtils.toByteArray(fileInputStream);
    }

    @PutMapping("/deleteFileByUUID")
    public ResponseEntity<SopResponse> deleteFileByUUID(@RequestParam("uuid") String uuid, @RequestParam("actionBy") String actionBy){

        return this.authorService.deleteFileByUUID(uuid, actionBy);
    }

    @GetMapping("/getActivityLog/{sopId}")
    public ResponseEntity<List<ActivityLogInterfaceDTO>> getActivityLog(@PathVariable("sopId") Integer sopId) {
        return ResponseEntity.ok(this.authorService.getActivityLog(sopId));
    }

    @GetMapping("/getRespDropDownDtls/{respId}")
    public ResponseEntity<ResponsibilityDTO> getRelatedRespDtls(@PathVariable("respId") Integer respId) {
        return ResponseEntity.ok(this.authorService.getRelatedRespDtls(respId));
    }

    @PutMapping("/updateRelatedStatus")
    public void updateRelatedStatus(@RequestParam("respId") Integer respId, @RequestParam("userId") String userId) {
        this.authorService.updateRelatedStatus(respId, userId);
    }

    @PutMapping("/updateRelatedRemarks")
    public void updateRelatedRemarks(@RequestParam("respId") Integer respId, @RequestParam("userId") String userId, @RequestParam("remarks") String remarks) {
        this.authorService.updateRelatedRemarks(respId, userId, remarks);
    }

    @GetMapping("/getRelatedRemarks/{respId}")
    public ResponseEntity<List<ViewSopDetailsInterfaceDTO>> getRelatedRemarks(@PathVariable("respId") Integer respId) {
        return this.authorService.getRelatedRemarks(respId);
    }

}
