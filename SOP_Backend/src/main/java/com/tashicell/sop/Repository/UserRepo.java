package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.User;
import com.tashicell.sop.Record.UserInfoInterfaceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String userName);
	
    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  sop_employee a \n" +
            "  INNER JOIN `sop_user_role_mapping` b\n" +
            "  ON a.`id` = b.`emp_id`\n" +
            "WHERE a.username = ?1", nativeQuery = true)
    Optional<User> findUserID(String userName);

    @Query(value = "SELECT\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(a.`first_name`),' ',IFNULL(a.`middle_name`, ''))),' ',LTRIM(a.`last_name`)) AS userName\n" +
            "FROM\n" +
            "  sop_employee a \n" +
            "WHERE a.username = ?1", nativeQuery = true)
    String getUserNameByID(String userId);

    @Query(value = "SELECT\n" +
            "  a.`id` AS userId,\n" +
            "  a.`username` AS empId,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(a.`first_name`),' ',IFNULL(a.`middle_name`, ''))),' ',LTRIM(a.`last_name`)) AS userName,  \n" +
            "  a.`mobile_no` AS phoneNo,\n" +
            "  a.`email_id` AS emailId,\n" +
            "  b.`role_id` AS roleId,\n" +
            "  a.`gender` AS gender\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "  LEFT JOIN `sop_user_role_mapping` b\n" +
            "    ON a.`id` = b.`emp_id`\n" +
            "WHERE b.`role_id` = 4 AND a.`status` = 'A'", nativeQuery = true)
    List<UserInfoInterfaceDTO> getEndorserList();

    @Query(value = "SELECT\n" +
            "  CASE\n" +
            "    WHEN COUNT(*) > 0\n" +
            "    THEN 'true'\n" +
            "    ELSE 'false'\n" +
            "  END\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "WHERE a.`email_id` = ?1",nativeQuery = true)
    Boolean emailExistCheck(String email);


    @Query(value = "SELECT\n" +
            "  CASE\n" +
            "    WHEN COUNT(*) > 0\n" +
            "    THEN 'true'\n" +
            "    ELSE 'false'\n" +
            "  END\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "WHERE a.`username` = ?1",nativeQuery = true)
    Boolean userCheck(String userName);

    @Transactional
    @Modifying
    @Query(value = "UPDATE `sop_employee` SET `password` = ?1 WHERE `username` = ?2", nativeQuery = true)
    void updatePasswordByID(String password, String username);

    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "  INNER JOIN `sop_user_role_mapping` b\n" +
            "    ON a.`id` = b.`emp_id`\n" +
            "WHERE b.`role_id` = 6 AND a.`status` = 'A'", nativeQuery = true)
    Optional<User> getAuthoriserDtls();

    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "  INNER JOIN `sop_user_role_mapping` b\n" +
            "  ON a.`id` = b.`emp_id`\n" +
            "WHERE a.`department_id` = ?1\n" +
            "  AND b.`role_id` = ?2 AND a.`status` = 'A'", nativeQuery = true)
    Optional<User> getReviewerDtls(Integer departmentId, Integer roleId);

    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "  INNER JOIN `sop_user_role_mapping` b\n" +
            "    ON a.`id` = b.`emp_id`\n" +
            "WHERE a.`department_id` = ?1\n" +
            "  AND b.`role_id` = 3 AND a.`status` = 'A'", nativeQuery = true)
    List<User> getFocalPersonDtls(Integer departmentId);

    @Query(value = "SELECT\n" +
            "  a.`mobile_no` AS phoneNo,\n" +
            "  a.`email_id` AS email,\n" +
            "  a.`gender` AS gender,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(a.`first_name`),' ',IFNULL(a.`middle_name`, ''))),' ',LTRIM(a.`last_name`)) AS userName\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "    ON a.`section_id` = b.`sec_id`\n" +
            "  LEFT JOIN `sop_user_role_mapping` c\n" +
            "    ON a.`id` = c.`emp_id`\n" +
            "WHERE b.`sec_id` = ?1\n" +
            "  AND b.`resp_id` = ?2\n" +
            "  AND c.`role_id` = 2\n" +
            "  AND a.`status` = 'A'", nativeQuery = true)
    UserInfoInterfaceDTO getRelatedSectionHead(Integer secId, Integer respID);

    @Query(value = "SELECT\n" +
            "  COUNT(*)\n" +
            "FROM\n" +
            "  `t_workflow_details` a\n" +
            "WHERE a.`stage_id` = 10", nativeQuery = true)
    Integer getFocalTaskListCount();


    @Query(value = "SELECT\n" +
            "  COUNT(*) AS reviewrCount\n" +
            "FROM\n" +
            "  `t_workflow_details` a\n" +
            "  LEFT JOIN `sop_title` b\n" +
            "  ON a.`sop_file_id` = b.`id`\n" +
            "WHERE a.`stage_id` IN (2,7) AND b.`dept_id` = ?1", nativeQuery = true)
    Integer getReviewerTaskListCount(Integer dept);

    @Query(value = "SELECT\n" +
            "  COUNT(*)\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "WHERE a.`stage_id` IN (9,13)\n" +
            "  AND a.`endorser_id` = ?1", nativeQuery = true)
    Integer getEndorserTaskListCount(Integer endorserId);

    @Query(value = "SELECT\n" +
            "  COUNT(*)\n" +
            "FROM\n" +
            "  `t_workflow_details` a\n" +
            "WHERE a.`stage_id` = 12", nativeQuery = true)
    Integer getAuthoriserTaskListCount();

    @Query(value = "SELECT\n" +
            "  a.`mobile_no` AS phoneNo,\n" +
            "  a.`email_id` AS email,\n" +
            "  a.`gender` AS gender,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(a.`first_name`),' ',IFNULL(a.`middle_name`, ''))),' ',LTRIM(a.`last_name`)) AS userName\n" +
            "FROM\n" +
            "  `sop_employee` a\n" +
            "  LEFT JOIN `sop_user_role_mapping` b\n" +
            "    ON a.`id` = b.`emp_id`\n" +
            "WHERE a.`section_id` = ?1\n" +
            "  AND b.`role_id` = ?2\n" +
            "  AND a.`status` = 'A'", nativeQuery = true)
    UserInfoInterfaceDTO getAuthorDetails(Integer secId, Integer roleId);
}
