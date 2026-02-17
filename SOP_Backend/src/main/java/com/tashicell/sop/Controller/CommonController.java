package com.tashicell.sop.Controller;

import com.tashicell.sop.Modal.User;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Common")
@RequiredArgsConstructor
public class CommonController {
	private final CommonService commonService;
	
	@GetMapping("/getSopType")
	public ResponseEntity<List<SopTypeRecordInterfaceDTO>> getSopType() {
		return ResponseEntity.ok(this.commonService.getSopType());
	}

	@GetMapping("/getDepartmentMaster")
	public ResponseEntity<List<DepartmentMasterInterface>> getDepartmentMaster() {
		return ResponseEntity.ok(this.commonService.getDepartmentMaster());
	}


	@GetMapping("/fetchStageMaster")
	public ResponseEntity<List<StageMasterInterface>> fetchStageMaster() {
		return ResponseEntity.ok(this.commonService.fetchStageMaster());
	}

	@GetMapping("/getSectionMaster")
	public ResponseEntity<List<SectionInterfaceDTO>> getSectionMaster() {
		return ResponseEntity.ok(this.commonService.getSectionMaster());
	}

	@GetMapping("/getSectionByDeptId/{deptId}")
	public ResponseEntity<List<SectionInterfaceDTO>> getSectionByDeptId(@PathVariable("deptId") Integer deptId) {
		return ResponseEntity.ok(this.commonService.getSectionByDeptId(deptId));
	}

	@GetMapping("/getRoleMaster")
	public ResponseEntity<List<RoleHolderMasterInterface>> getRoleMaster() {
		return ResponseEntity.ok(this.commonService.getRoleMaster());
	}

	@GetMapping("/getRespNameDropDown/{userId}")
	public ResponseEntity<List<RoleHolderMasterInterface>> getRoleHolderMaster(@PathVariable("userId") String empId) {
		return ResponseEntity.ok(this.commonService.getResponsibilityDtls(empId));
	}

	@GetMapping("/getTaskListCount/{userId}")
	public ResponseEntity<PendingTaskListCount> getFocalSopCount(@PathVariable("userId") String empId) {

		return this.commonService.getFocalSopCount(empId);
	}

	@GetMapping("/getUserDetls/{userId}")
	public ResponseEntity<UserDetailsDto> getUserDetls(@PathVariable("userId") String empId) {

		return this.commonService.fetchUserDtls(empId);
	}

	@GetMapping("/getSopCount/{userId}")
	public ResponseEntity<Integer> getSopCount(@PathVariable("userId") String empId) {
		return this.commonService.getSopCount(empId);
	}
}
