package com.tashicell.sop.Service;

import com.tashicell.sop.Record.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserManagementService {
    SSOEmpResponseDTO getUserDetails(String empId);

    AuthenticationResponse addUser(UserDetailsDto userDetailsDto);

    UserDetailsDto editUser(String empID);

    List<UserDetailsDto> getAllUser();

    String updateUser(UserDetailsDto userDetailsDto);

    ResponseEntity<SopResponse> editUserStatus(String empID);

    SSO_DTO getTokenVerification(String token);

    void ssoUserStatus(HttpServletResponse response, String empId, Integer userStatus);

    void userPasswordUpdate(HttpServletResponse response, String empID, String newPassword);
}
