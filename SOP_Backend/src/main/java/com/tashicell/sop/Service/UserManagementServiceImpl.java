package com.tashicell.sop.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tashicell.sop.Exception.RecordNotFoundException;
import com.tashicell.sop.Modal.*;
import com.tashicell.sop.Record.*;
import com.tashicell.sop.Repository.*;
import com.tashicell.sop.Utility.MailSender;
import com.tashicell.sop.Utility.PemUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepo userRepo;

    private final RoleRepo roleRepo;

    private final SectionRepo sectionRepo;

    private final PasswordEncoder passwordEncoder;

    private final DepartmentRepo departmentRepo;

    private final DesignationRepo designationRepo;

    private final CommonServiceImpl commonService;


    @Override
    public SSO_DTO getTokenVerification(String token) {
        SSO_DTO ssoDto = new SSO_DTO();
        try{

            SimpleDateFormat date = new SimpleDateFormat("yyyy_MM_dd");
            String arr[] = token.split("\\|");
            String tokenEmpID = arr[0];
            String tokenSignedData = arr[1];
            String tokenSignature = arr[2];
            String tokenEncryptedPass = arr[3];

            String tokenSignedDataJava = tokenSignedData.substring(3,tokenSignedData.length());
            tokenSignedDataJava = "$2a"+tokenSignedDataJava;

            int empID = (Integer.parseInt(tokenEmpID))/9;

            String empIDString = String.valueOf(empID);
            int empIDleng = empIDString.length();
            String employeeID = "";

            if (empIDleng >= 3){
                employeeID = "E00" + empID;
            }else if (empIDleng == 2){
                employeeID = "E000" + empID;
            }else if (empIDleng == 1){
                employeeID = "E0000" + empID;
            }

            User user = commonService.getUserDtls(employeeID);

            String mobNo = user.getMobileNo();

            if (!mobNo.isEmpty()){
                String payLoadClient = empID + "@" + mobNo + "@" + date.format(new Date());

                /*String payLoadClient = empID + "@" + mobNo + "@2023_10_25_45";*/
                Boolean bcryptVerification = BCrypt.checkpw(payLoadClient, tokenSignedDataJava);


                ResourceBundle resourceBundle = ResourceBundle.getBundle("documentUploads");
                String filePathPrefix = resourceBundle.getString("publicKeyFileStore");
                String publicKeyPath = filePathPrefix+"/SOP_SSO.pem";

                PublicKey ssoKey = PemUtils.readPublicKeyFromFile(publicKeyPath, "RSA");


                Boolean tokenVerification = verify(ssoKey, tokenSignedData, tokenSignature);


                byte[] tokenDecryptedPass = Base64.getDecoder().decode(tokenEncryptedPass);
                String token_Password = new String(tokenDecryptedPass);

                String firstPartPass = (empID*2)+"!2#$@";
                String secondPartPass = "@2345"+(empID*2);

                String almostPassword = token_Password.replace(firstPartPass,"");
                String tokenPass = almostPassword.replace(secondPartPass, "");

                if(bcryptVerification && tokenVerification){

                    ssoDto.setEmpID(employeeID);
                    ssoDto.setPassword(tokenPass);
                    ssoDto.setSuccess(true);

                }else {
                    ssoDto.setSuccess(false);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return ssoDto;
    }



    public static boolean verify(PublicKey publicKey,String signedData,String signature){
        Signature sig;
        try {
            sig=Signature.getInstance("SHA1withRSA");
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());

            byte[] decodedHex = org.apache.commons.codec.binary.Hex.decodeHex(signature.toCharArray());
            String result = Base64.getEncoder().encodeToString(decodedHex);

            if (!sig.verify(Base64.getDecoder().decode(result))) {
                return false;
            }
            return true;
        }
        catch (  Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public SSOEmpResponseDTO getUserDetails(String empId) {

        SSOEmpResponseDTO ssoEmpDetails = new SSOEmpResponseDTO();

        String userName = userRepo.getUserNameByID(empId);
        if (userName == null || userName == ""){

            ResourceBundle resourceBundle1 = ResourceBundle.getBundle("wsEndPointURL_en_US");
            String getEmpDetailsURL =resourceBundle1.getString("getPMSEmployee.endPointURL");

            String username = resourceBundle1.getString("CONSUMER.KEY");
            String password = resourceBundle1.getString("CONSUMER.SECRET");

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();

            headers.set("Content-Type", "application/json");
            
            headers.setBasicAuth(username,password);

            HttpEntity<?> request = new HttpEntity(headers);	

            String userID = empId.substring(3);

            ResponseEntity<SSOEmpResponseDTO> response = restTemplate.exchange(getEmpDetailsURL+userID, HttpMethod.GET, request, SSOEmpResponseDTO.class);

            if (response.getStatusCode().value() == 200) {

                ssoEmpDetails = response.getBody();
            }else{
                ssoEmpDetails.setStatus("Failed");
            }
        }else {
            ssoEmpDetails.setStatus("Failed");
        }

        return ssoEmpDetails;

    }

    @Override
    public AuthenticationResponse addUser(UserDetailsDto userDetailsDto) {

        AuthenticationResponse response = new AuthenticationResponse();

        try {
            if (userDetailsDto != null) {
                Boolean userCheck = userRepo.userCheck(userDetailsDto.getUserName());
                if (!userCheck) {
                    String password = userDetailsDto.getPassword();
                    DepartmentMaster departmentMaster = getDepartmentId(userDetailsDto.getDepartment());
                    SectionMaster sectionMaster = getSectionId(userDetailsDto.getSection());
                    DesignationMaster designationMaster = getDesignationId(userDetailsDto.getDesignation());
                    RoleMaster roleId = getRoleId(userDetailsDto.getRoleId());

                        var user = User.builder()
                            .firstName(userDetailsDto.getFirstName())
                            .lastName(userDetailsDto.getLastName())
                            .middleName(userDetailsDto.getMiddleName())
                            .emailId(userDetailsDto.getEmail())
                            .mobileNo(userDetailsDto.getMobileNo())
                            .username(userDetailsDto.getUserName())
                            .departmentMaster(departmentMaster)
                            .sectionMaster(sectionMaster)
                            .designationMaster(designationMaster)
                            .role(roleId)
                            .gender(userDetailsDto.getGender())
                            .password(passwordEncoder.encode(userDetailsDto.getPassword()))
                            .createdBy(userDetailsDto.getCreatedBy())
                            .status('A')
                            .build();
                        var savedUser = userRepo.save(user);

                        if (savedUser != null) {
                            //Update user to SSO System
                            SSOEmpResponseDTO ssoEmpDetails = new SSOEmpResponseDTO();
                            ResourceBundle resourceBundle1 = ResourceBundle.getBundle("wsEndPointURL_en_US");
                            String getEmpDetailsURL = resourceBundle1.getString("updateSSOEmpAccess.endPointURL");

                            String consumer_username = resourceBundle1.getString("CONSUMER.KEY");
                            String consumer_password = resourceBundle1.getString("CONSUMER.SECRET");
                            String appCode = resourceBundle1.getString("ssoAPIappCode");
                            String access = resourceBundle1.getString("ssoAccessEnable");

                            RestTemplate restTemplate = new RestTemplate();

                            HttpHeaders headers = new HttpHeaders();

                            headers.set("Content-Type", "application/json");
                            headers.set("Method", "POST");

                            headers.setBasicAuth(consumer_username, consumer_password);

                            String userID = userDetailsDto.getUserName().substring(3);

                            SSOAceessDTO ssoAceessDTO = new SSOAceessDTO(userID, access, appCode);

                            ObjectMapper Obj = new ObjectMapper();
                            String jsonStr = "";

                            try {
                                jsonStr = Obj.writeValueAsString(ssoAceessDTO);
                            } catch (IOException e) {
                                e.printStackTrace();
                                ssoEmpDetails.setMessage(e.getMessage());
                            }

                            try {
                                HttpEntity<String> requestEntity = new HttpEntity<>(jsonStr, headers);

                                ssoEmpDetails = restTemplate.postForObject(getEmpDetailsURL, requestEntity, SSOEmpResponseDTO.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ssoEmpDetails.setMessage(e.getMessage());
                            }

                            if (ssoEmpDetails.getCode().equals(1)) {
                                String gender = "";
                                if (userDetailsDto.getGender().charValue() == 'M'){
                                    gender = "Mr";
                                }else {
                                    gender = "Ms";
                                }
                                String mailContent =
                                        "<html> <body>" + "Dear" +gender + " "  + userDetailsDto.getFirstName() + ",<br>"
                                                + "Please login from SSO to access the SOP System with your PMS credentials.<br>"
                                                //+ "Please login from SSO to access the SOP System with user ID is " + userDetailsDto.getUserName() + " and password is " + password + ".<br>"
                                                + "<p>Thank You</p>"
                                                + "</body></html>";
                                try {
                                    boolean isSend = MailSender.sendMail(userDetailsDto.getEmail(), null, null, mailContent, "User credentials");

                                    if (isSend) {
                                        response.setSuccess(true);
                                    } else {
                                        response.setSuccess(false);
                                        throw new RecordNotFoundException("Error Sending email");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    throw new RecordNotFoundException("Error: "+e.getMessage());
                                }
                            }else {
                                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
                            }
                        } else {
                            response.setSuccess(false);
                            TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
                        }
                } else {
                    response.setSuccess(false);
                    throw new RecordNotFoundException("Already user Exist");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            response.setSuccess(false);
            throw new RecordNotFoundException("Error: "+e.getMessage());
        }
        return AuthenticationResponse.builder()
                .success(true)
                .build();
    }

    @Override
    public UserDetailsDto editUser(String empID) {
        UserDetailsDto userDetailsDto = new UserDetailsDto();

        try {
            User user= userRepo.findUserID(empID)
                    .orElseThrow(() -> new IllegalArgumentException("User Details not found with ID: " + empID));

            if (user != null){
                userDetailsDto.setUserName(empID);
                userDetailsDto.setFirstName(user.getFirstName());
                userDetailsDto.setMiddleName(user.getMiddleName());
                userDetailsDto.setLastName(user.getLastName());
                userDetailsDto.setEmail(user.getEmailId());
                userDetailsDto.setMobileNo(user.getMobileNo());
                userDetailsDto.setEmail(user.getEmailId());
                userDetailsDto.setDesignation(user.getDesignationMaster().getId());
                userDetailsDto.setDesignationName(user.getDesignationMaster().getDesignationName());
                userDetailsDto.setDepartment(user.getDepartmentMaster().getId());
                userDetailsDto.setDepartmentName(user.getDepartmentMaster().getDepartmentName());
                userDetailsDto.setSection(user.getSectionMaster().getId());
                userDetailsDto.setSectionName(user.getSectionMaster().getSectionName());
                userDetailsDto.setRoleId(user.getRole().getId());
                userDetailsDto.setRole(user.getRole().getRoleName());
            }

        }catch (Exception e){
            log.error(e.getMessage());
            throw new RecordNotFoundException("Error: "+e.getMessage());
        }
        return userDetailsDto;
    }

    @Override
    public List<UserDetailsDto> getAllUser() {
        List<UserDetailsDto> userDetailsDtoList = new ArrayList<>();
        try {
            List<User> userList = userRepo.findAll();
            if (!CollectionUtils.isEmpty(userList)){ 
                for (User userDtls : userList){
                    UserDetailsDto userDetails = new UserDetailsDto();
                    userDetails.setUserName(userDtls.getUsername());
                    userDetails.setFirstName(userDtls.getFirstName());
                    userDetails.setMiddleName(userDtls.getMiddleName());
                    userDetails.setLastName(userDtls.getLastName());
                    userDetails.setEmail(userDtls.getEmailId());
                    userDetails.setMobileNo(userDtls.getMobileNo());
                    userDetails.setEmail(userDtls.getEmailId());
                    userDetails.setDesignation(userDtls.getDesignationMaster().getId());
                    userDetails.setDesignationName(userDtls.getDesignationMaster().getDesignationName());
                    userDetails.setDepartment(userDtls.getDepartmentMaster().getId());
                    userDetails.setDepartmentName(userDtls.getDepartmentMaster().getDepartmentName());
                    userDetails.setSection(userDtls.getSectionMaster().getId());
                    userDetails.setSectionName(userDtls.getSectionMaster().getSectionName());
                    userDetails.setRoleId(userDtls.getRole().getId());
                    userDetails.setRole(userDtls.getRole().getRoleName());
                    userDetails.setUserStatus(userDtls.getStatus()); 

                    userDetailsDtoList.add(userDetails);
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RecordNotFoundException("Error: "+e.getMessage());
        }
        return userDetailsDtoList;
    }

    @Override
    public String updateUser(UserDetailsDto userDetailsDto) {
        String status = "FAILED";

        try {
            if (userDetailsDto != null){

                User userEntity = commonService.getUserDtls(userDetailsDto.getUserName());
                if (userEntity != null){

                    DepartmentMaster departmentMaster = getDepartmentId(userDetailsDto.getDepartment());
                    SectionMaster sectionMaster = getSectionId(userDetailsDto.getSection());
                    DesignationMaster designationMaster = getDesignationId(userDetailsDto.getDesignation());
                    RoleMaster roleId = getRoleId(userDetailsDto.getRoleId());

                    if (userDetailsDto.getDepartment() != null){
                        userEntity.setDepartmentMaster(departmentMaster);
                    }
                    if (userDetailsDto.getSection() != null){
                        userEntity.setSectionMaster(sectionMaster);
                    }
                    if (userDetailsDto.getDesignation() != null){
                        userEntity.setDesignationMaster(designationMaster);
                    }
                    if (userDetailsDto.getRoleId() != null){
                        userEntity.setRole(roleId);
                    }
                    if (userDetailsDto.getMobileNo() != null){
                        userEntity.setMobileNo(userDetailsDto.getMobileNo());
                    }
                    if (userDetailsDto.getEmail() != null){
                        userEntity.setEmailId(userDetailsDto.getEmail());
                    }
                    userEntity.setUpdatedBy(commonService.getUserDtls(userDetailsDto.getUpdatedBy()));

                    User userStatus = this.userRepo.save(userEntity);

                    if(userStatus != null){
                        status = "User details has been successfully updated";
                    }else {
                        status = "User details failed to updated!!";
                    }


                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RecordNotFoundException("Error: "+e.getMessage());
        }

        return status;
    }

    @Override
    public ResponseEntity<SopResponse> editUserStatus(String empID) {
        User user = commonService.getUserDtls(empID);
        SopResponse sopResponse = new SopResponse();

        SSOEmpResponseDTO ssoEmpDetails = new SSOEmpResponseDTO();
        String ssoAccessCode;
        ResourceBundle resourceBundle1 = ResourceBundle.getBundle("wsEndPointURL_en_US");
        String getEmpDetailsURL =resourceBundle1.getString("updateSSOEmpAccess.endPointURL");

        String username = resourceBundle1.getString("CONSUMER.KEY");
        String password = resourceBundle1.getString("CONSUMER.SECRET");
        String appCode = resourceBundle1.getString("ssoAPIappCode");
        if (user != null){
            if (user.getStatus().equals('A')){
                user.setStatus('D');
                ssoAccessCode = resourceBundle1.getString("ssoAccessDisable");
            }else {
                user.setStatus('A');
                ssoAccessCode = resourceBundle1.getString("ssoAccessEnable");
            }

            userRepo.save(user);

            //SSO user status update
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();

            headers.set("Content-Type", "application/json");
            headers.set("Method", "POST");

            headers.setBasicAuth(username,password);

            String userID = empID.substring(3);

            SSOAceessDTO ssoAceessDTO = new SSOAceessDTO();
            ssoAceessDTO.setEmpId(userID);
            ssoAceessDTO.setAppCode(appCode);
            ssoAceessDTO.setAccess(ssoAccessCode);

            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = "";

            try {
                jsonStr = Obj.writeValueAsString(ssoAceessDTO);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                HttpEntity<String> requestEntity = new HttpEntity<>(jsonStr, headers);

                ssoEmpDetails = restTemplate.postForObject(getEmpDetailsURL, requestEntity, SSOEmpResponseDTO.class);

            }catch (Exception e){
                e.printStackTrace();
            }
            if (ssoEmpDetails.getCode().equals(1)) {
                sopResponse.setSuccess(true);
                sopResponse.setResponseCode(1);
                sopResponse.setResponseText("Successfully updated the user status");
            }else {
                sopResponse.setSuccess(false);
                sopResponse.setResponseCode(0);
                sopResponse.setResponseText("Failed to updated the user status");
            }

        }else {
            sopResponse.setSuccess(false);
        }
        return ResponseEntity.ok(sopResponse);
    }

    private DepartmentMaster getDepartmentId(Integer department) {
        return this.departmentRepo.findById(department)
                .orElseThrow(() -> new IllegalArgumentException("Department ID not found with ID: " +department));
    }
    private SectionMaster getSectionId(Integer section) {
        return this.sectionRepo.findById(section)
                .orElseThrow(() -> new IllegalArgumentException("Section ID not found with ID: " +section));
    }
    private DesignationMaster getDesignationId(Integer designation) {
        return this.designationRepo.findById(designation)
                .orElseThrow(() -> new IllegalArgumentException("Designation ID not found with ID: " +designation));
    }
    private RoleMaster getRoleId(Integer roleId) {
        return this.roleRepo.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role ID not found with ID: " +roleId));
    }

    @Override
    public void ssoUserStatus(HttpServletResponse response, String empId, Integer userStatus) {
        String userID = "E00"+empId;
        String result = "";
        JSONObject json = new JSONObject();
        try {

            User user = commonService.getUserDtls(userID);
            if (user != null) {
                if (userStatus.equals(1)){
                    user.setStatus('A');
                }else {
                    user.setStatus('D');
                }
                userRepo.save(user);
                json.put("success", true);
            }else {
                json.put("success", false);
                json.put("reason", result);
                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
            }

        }catch (Exception e){
            json.put("reason", e.getMessage());
        }
        try {
            response.setContentType("application/json");
            response.getWriter().write(json.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    public void userPasswordUpdate(HttpServletResponse response, String empID, String newPassword) {
        String userID = "E00"+empID;
        User userDetails = commonService.getUserDtls(userID);

        JSONObject json = new JSONObject();
        try {
            if (userDetails.getPassword().isEmpty()){
                json.put("success", false);
            }else {
                userDetails.setPassword(passwordEncoder.encode(newPassword));
                /*userRepo.updatePasswordByID(passwordEncoder.encode(newPassword),userID);*/
                userRepo.save(userDetails);
                json.put("success", true);
            }
        }catch (Exception e){
            json.put("success", false);
            json.put("reason", e.getMessage());
        }
        try {
            response.setContentType("application/json");
            response.getWriter().write(json.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
