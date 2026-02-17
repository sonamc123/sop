package com.tashicell.sop.Controller;


import com.tashicell.sop.Record.SSO_DTO;
import com.tashicell.sop.Service.UserManagementService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sopToken")
@RequiredArgsConstructor
public class SsoController {

    private final UserManagementService userManagementService;

    @GetMapping("/sso")
    public ResponseEntity<SSO_DTO> getTokenVerification(@RequestParam("token") String token){
        return ResponseEntity.ok().body(userManagementService.getTokenVerification(token));
    }

    //SSO Application access/deny status updated
    @PutMapping("/ssoUserStatus")
    public void ssoUserStatus(HttpServletResponse response, @RequestParam("empID") String empId, @RequestParam("userStatus") Integer userStatus) {
        this.userManagementService.ssoUserStatus(response, empId, userStatus);

    }

    //SSO Application user password update
    @PutMapping("userPasswordUpdate")
    public void userPasswordUpdate(HttpServletResponse response, @RequestParam("empID") String empID){
        String newPassword = "1993-06-30";
        this.userManagementService.userPasswordUpdate(response, empID,newPassword);

    }
}
