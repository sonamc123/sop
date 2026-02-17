package com.tashicell.sop.Service;

import com.tashicell.sop.Modal.*;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CommonServiceImpl implements CommonService{

    private final UserRepo userRepo;
    private final SopTypeMasterRepo sopTypeMasterRepo;

    private final DepartmentRepo departmentRepo;
    private final SectionRepo sectionRepo;

    private final RoleRepo roleRepo;

    private final DesignationRepo designationRepo;

    private final SopFileRepo sopFileRepo;

    @Override
    public List<SopTypeRecordInterfaceDTO> getSopType() {

        return this.sopTypeMasterRepo.getSopTypeDetails();
    }

    @Override
    public List<DepartmentMasterInterface> getDepartmentMaster() {

        return this.departmentRepo.getDepartmentMaster();
    }

    @Override
    public List<SectionInterfaceDTO> getSectionMaster() {

        return this.sectionRepo.getSectionMaster();
    }

    @Override
    public List<SectionInterfaceDTO> getSectionByDeptId(Integer deptId) {

        return this.sectionRepo.getSectionByDeptId(deptId);
    }

    @Override
    public List<RoleHolderMasterInterface> getResponsibilityDtls(String empId) {
        User userDtls = getUserDtls(empId);
        Integer secID = userDtls.getSectionMaster().getId();
        return this.sectionRepo.getResponsibilityDtls(secID);
    }

    @Override
    public List<RoleHolderMasterInterface> getRoleMaster() {
        return this.roleRepo.getRoleMaster();
    }

    @Override
    public ResponseEntity<PendingTaskListCount> getFocalSopCount(String empId) {
        User user = getUserDtls(empId);
        Integer dept = user.getDepartmentMaster().getId();
        Integer roleId = user.getRole().getId();
        Integer endorserId= user.getId();

        PendingTaskListCount pendingTaskListCount = new PendingTaskListCount();

        if (roleId == 3){
            Integer focalCount = userRepo.getFocalTaskListCount();
            pendingTaskListCount.setCount(focalCount);
        }else if (roleId ==4){
            Integer reviewerCount = userRepo.getReviewerTaskListCount(dept);
            Integer endorserCount = userRepo.getEndorserTaskListCount(endorserId);

            pendingTaskListCount.setCount(endorserCount);
            pendingTaskListCount.setReviewerCount(reviewerCount);

        }else if (roleId ==6){
            Integer authoriserCount = userRepo.getAuthoriserTaskListCount();

            pendingTaskListCount.setCount(authoriserCount);

        }else if (roleId ==7){
            Integer reviewerCount = userRepo.getReviewerTaskListCount(dept);
            pendingTaskListCount.setCount(reviewerCount);
        }

        return ResponseEntity.ok(pendingTaskListCount);
    }

    public User getUserDtls(String userID){
        return this.userRepo.findUserID(userID)
                .orElseThrow(() -> new IllegalArgumentException("User Details not found with ID: " + userID));
    }

    @Override
    public ResponseEntity<UserDetailsDto> fetchUserDtls(String empId) {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        User useDtls = getUserDtls(empId);
        if (useDtls != null){
            if (useDtls.getMiddleName() == null ){
                useDtls.setMiddleName("");
            }
            if (useDtls.getLastName() == null ){
                useDtls.setLastName("");
            }
            userDetailsDto.setUserName(useDtls.getFirstName() +" "+ useDtls.getMiddleName() +" "+ useDtls.getLastName());
            userDetailsDto.setDesignationName(useDtls.getDesignationMaster().getDesignationName());
            userDetailsDto.setDepartmentName(useDtls.getDepartmentMaster().getDepartmentShortCode());
            userDetailsDto.setSectionName(useDtls.getDepartmentMaster().getDepartmentName());


        }
        return ResponseEntity.ok().body(userDetailsDto);
    }

    @Override
    public ResponseEntity<Integer> getSopCount(String empId) {

        User userDetails = getUserDtls(empId);
        Integer secId = userDetails.getSectionMaster().getId();

        Integer sopCount = sopFileRepo.getSopCount(secId);
        return ResponseEntity.ok().body(sopCount);
    }

    @Override
    public List<StageMasterInterface> fetchStageMaster() {
        return this.departmentRepo.fetchStageMaster();
    }

    public DepartmentMaster getDeptId(Integer deptId){
        return this.departmentRepo.findById(deptId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + deptId));

    }

    public SectionMaster getSecId(Integer secId){
        return this.sectionRepo.findById(secId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + secId));
    }

    public DesignationMaster getRoleHolderId(Integer roleHolderId){
        return this.designationRepo.findById(roleHolderId)
                .orElseThrow(() -> new IllegalArgumentException("RoleHolder not found with ID: " + roleHolderId));
    }


    public void getWorkFlowDtls(String remark, StageMaster stageMaster, User user, SopFileDetails sopFileDetails, WorkFlowDetails workFlowDetails, SopWorkFLowRepo workFLowRepo, LogRepo logRepo) {

            workFlowDetails.setStageMaster(stageMaster);
            workFlowDetails.setActionTakenBy(user);
            workFlowDetails.setRemarks(remark);
            workFlowDetails.setActionTakenOn(LocalDateTime.now());

            if (stageMaster.getId() == 8){
                workFlowDetails.setIsEndorsed(stageMaster);
                workFlowDetails.setSopVersion(sopFileDetails.getSopVersion());
            }

            //Save in workflow.
            workFLowRepo.save(workFlowDetails);


    }

}
