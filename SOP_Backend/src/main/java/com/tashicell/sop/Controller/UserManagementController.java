package com.tashicell.sop.Controller;

import com.tashicell.sop.Record.*;
import com.tashicell.sop.Service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/UserManagement")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/getEmpDetailSSO/{empID}")
    public ResponseEntity<SSOEmpResponseDTO> getUserDetails(@PathVariable("empID") String empId){
        return ResponseEntity.ok().body(userManagementService.getUserDetails(empId));
    }

    @PostMapping("/addUser")
    public ResponseEntity<AuthenticationResponse> addUser(@RequestBody UserDetailsDto userDetailsDto){
        return ResponseEntity.ok().body(userManagementService.addUser(userDetailsDto));
    }
    @GetMapping("getAllUsers")
    public ResponseEntity<List<UserDetailsDto>> getAllUser(){
        return ResponseEntity.ok().body(userManagementService.getAllUser());
    }

    @GetMapping("/getUser/{empID}")
    public ResponseEntity<UserDetailsDto> editUser(@PathVariable("empID") String empID){
        return ResponseEntity.ok().body(userManagementService.editUser(empID));
    }

    @PutMapping("/updateUser")
    public ResponseEntity<String> updateUser(@RequestBody UserDetailsDto userDetailsDto){
        /*URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/UserManagement/updateUser").toUriString());*/
        return ResponseEntity.ok().body(userManagementService.updateUser(userDetailsDto));

    }

    @PutMapping("/editStatus/{empID}")
    public ResponseEntity<SopResponse> editStatus(@PathVariable("empID") String empID) {
        return this.userManagementService.editUserStatus(empID);

    }

}
