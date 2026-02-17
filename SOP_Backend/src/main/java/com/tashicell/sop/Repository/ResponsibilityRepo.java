package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.Responsibilities;
import com.tashicell.sop.Record.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResponsibilityRepo extends JpaRepository<Responsibilities, Integer> {

    @Query(value = "SELECT\n" +
            "  a.`department_id_id`\n" +
            "FROM\n" +
            "  `sop_section` a\n" +
            "WHERE a.`id` = ?1", nativeQuery = true)
    Integer getDeptByID(Integer sectionId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId,\n" +
            "  a.`responsibility_name` AS responsibilityName,\n" +
            "  a.`want_to_delete` AS respDelete,\n" +
            "  GROUP_CONCAT(DISTINCT(s.`section_name`)) AS secList,\n" +
            "  GROUP_CONCAT(DISTINCT(c.`department_short_code`)) AS deptName,\n" +
            "  CASE\n" +
            "    WHEN b.`status` = 'P'\n" +
            "    THEN 'Pending'\n" +
            "    WHEN b.`status` = 'A'\n" +
            "    THEN 'Agreed'\n" +
            "    WHEN b.`status` = 'D'\n" +
            "    THEN 'Disagreed'\n" +
            "  END AS relatedStatus,\n" +
            "  b.`remarks` AS relatedRemarks\n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "    ON a.`res_id` = b.`resp_id`\n" +
            "  LEFT JOIN `sop_department` c\n" +
            "    ON b.`dept_id` = c.`id`\n" +
            "  LEFT JOIN `t_workflow_details` e\n" +
            "    ON e.`sop_file_id` = a.`sop_id`\n" +
            "  LEFT JOIN `sop_section` s\n" +
            "    ON b.`sec_id` = s.`id`\n" +
            "WHERE a.`sop_id` = ?1\n" +
            "GROUP BY a.`res_id`", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> viewResponsibilitiesLstBySop(Integer sopId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId,\n" +
            "  a.`responsibility_name` AS responsibilityName,\n" +
            "  rl.`id` AS roleHolder,\n" +
            "  rl.`designation_name` AS roleHolderName,\n" +
            "  a.`content` AS content,\n" +
            "  a.`remarks` AS remarks,\n" +
            "  a.`want_to_delete` AS respDelete,\n" +
            "  GROUP_CONCAT(DISTINCT(b.`sec_id`)) AS secList,\n" +
            "  GROUP_CONCAT(\n" +
            "    DISTINCT CONCAT(\n" +
            "      s.`section_name`,\n" +
            "      '(',\n" +
            "      CASE\n" +
            "        WHEN b.`status` = 'A'\n" +
            "        THEN 'Agreed'\n" +
            "        WHEN b.`status` = 'P'\n" +
            "        THEN 'Pending'\n" +
            "        WHEN b.`status` = 'D'\n" +
            "        THEN 'Disagreed'\n" +
            "      END,\n" +
            "      ')'\n" +
            "    )\n" +
            "  ) AS secNameList,\n" +
            "  CASE\n" +
            "  WHEN b.`status` = 'P'\n" +
            "  THEN 'Pending'\n" +
            "  WHEN b.`status` = 'A'\n" +
            "  THEN 'Agreed'\n" +
            "  WHEN b.`status` = 'D'\n" +
            "  THEN 'Disagreed'\n" +
            "END AS relatedStatus,\n" +
            "  GROUP_CONCAT(\n" +
            "    DISTINCT (c.`department_short_code`)\n" +
            "  ) AS deptName,\n" +
            "  (SELECT\n" +
            "    d.`content`\n" +
            "  FROM\n" +
            "    `sop_responsibilities_audit` d\n" +
            "    LEFT JOIN `sop_responsibilities` e\n" +
            "      ON d.`res_id` = e.`res_id`\n" +
            "    LEFT JOIN `sop_title` f\n" +
            "      ON e.`sop_id` = f.`id`\n" +
            "  WHERE d.`res_id` = a.`res_id`\n" +
            "    AND e.`type_id` = f.`update_type`\n" +
            "  ORDER BY d.`updated_on` DESC\n" +
            "  LIMIT 1) AS oldContent,\n" +
            "  r.`id` AS roleId\n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "    ON a.`res_id` = b.`resp_id`\n" +
            "  LEFT JOIN `sop_department` c\n" +
            "    ON b.`dept_id` = c.`id`\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = a.`role_holder`\n" +
            "  LEFT JOIN `sop_title` sop\n" +
            "    ON a.`sop_id` = sop.`id`\n" +
            "  LEFT JOIN `sop_user_role_mapping` rm\n" +
            "    ON sop.`created_by` = rm.`emp_id`\n" +
            "  LEFT JOIN `sop_role_master` r\n" +
            "    ON rm.`role_id` = r.`id`\n" +
            "  LEFT JOIN `sop_section` s\n" +
            "    ON b.`sec_id` = s.`id`\n" +
            "WHERE a.`res_id` = ?1", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> viewResponsibilitiesLstDtlsByRespId(Integer respId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId,\n" +
            "  a.`responsibility_name` AS responsibilityName,\n" +
            "  rl.`id` AS roleHolder,\n" +
            "  rl.`designation_name` AS roleHolderName,\n" +
            "  a.`content` AS content,\n" +
            "  a.`type_id` AS sopTypeId,\n" +
            "  a.`remarks` AS remarks,\n" +
            "  GROUP_CONCAT(DISTINCT(b.`sec_id`)) AS secList,  \n" +
            "  GROUP_CONCAT(DISTINCT(c.`department_short_code`)) AS deptName,\n" +
            "  rf.`resp_related_from` AS respIdFrom,\n" +
            "  rf.`resp_related_to` AS respIdTo\n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "    ON a.`res_id` = b.`resp_id`\n" +
            "  LEFT JOIN `sop_department` c\n" +
            "    ON b.`dept_id` = c.`id`\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = a.`role_holder`\n" +
            "  LEFT JOIN `sop_inter_related_resp` rf\n" +
            "    ON a.`res_id` = rf.`resp_related_from`\n" +
            "WHERE a.`res_id` = ?1", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> viewResponsibilitiesLstDtlsViewer(Integer respId);

    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping`\n" +
            "WHERE `resp_id` = ?1",nativeQuery = true)
    void deleteSecMappingByRespId(Integer respId);

    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_file_attachment`\n" +
            "WHERE `sop_resp_id` = ?1",nativeQuery = true)
    void deleteDocMappingByRespId(Integer respId);

    @Query(value = "SELECT\n" +
            "  dep.`department_name` AS departmentName,\n" +
            "  sec.`section_name` AS sopTitle,\n" +
            "  (SELECT a.`current_sop_no` FROM `sop_history_dtls` a WHERE a.`sop_no` = sop.`id` AND a.`is_endorsed` = 'Y' ORDER BY a.`effective_date` DESC LIMIT 1) AS sopNumber,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(au.`first_name`),' ',IFNULL(au.`middle_name`, ''))),' ',LTRIM(au.`last_name`)) AS authorName,\n" +
            "  dg.`designation_name` AS authorDesignation,\n" +
            "  DATE_FORMAT(sop.`created_on`, '%D %b, %Y at %l:%i %p') AS createdOn,  \n" +
            "  (SELECT CONCAT(RTRIM(CONCAT(RTRIM(re.`first_name`),' ',IFNULL(re.`middle_name`, ''))),' ',LTRIM(re.`last_name`)) FROM `t_workflow_details_audit` wk1 LEFT JOIN `sop_employee` re ON wk1.`action_taken_by` = re.`id` WHERE wk1.`sop_file_id` = sop.`id` AND wk1.`stage_id` = 4 ORDER BY wk1.`action_taken_on` ASC LIMIT 1) AS reviewerName, \n" +
            "  (SELECT dg2.`designation_name` FROM `t_workflow_details_audit` wka LEFT JOIN `sop_employee` empR ON wka.`action_taken_by` = empR.`id` LEFT JOIN `sop_designation` dg2 ON empR.`designation` = dg2.`id` WHERE wka.`sop_file_id` = sop.`id` AND wka.`stage_id` = 4 ORDER BY wka.`action_taken_on` ASC LIMIT 1) AS reviewerDesignation,\n" +
            "  (SELECT DATE_FORMAT(wk2.`action_taken_on`, '%D %b, %Y at %l:%i %p') FROM `t_workflow_details_audit` wk2 WHERE wk2.`sop_file_id` = sop.`id` AND wk2.`stage_id` IN (4) ORDER BY wk2.`action_taken_on` ASC LIMIT 1) AS reviewedOn, \n" +
            /*"  (SELECT CONCAT(RTRIM(CONCAT(RTRIM(en.`first_name`),' ',IFNULL(en.`middle_name`, ''))),' ',LTRIM(en.`last_name`)) FROM `t_workflow_details` wk LEFT JOIN `sop_employee` en ON wk.`action_taken_by` = en.`id` WHERE wk.`sop_file_id` = sop.`id` AND wk.`is_endorsed` = 8 ORDER BY wk.`action_taken_on` ASC LIMIT 1) AS endorserName, \n" +*/
            "  (SELECT CONCAT(RTRIM(CONCAT(RTRIM(en.`first_name`),' ',IFNULL(en.`middle_name`, ''))),' ',LTRIM(en.`last_name`))FROM `sop_employee` en LEFT JOIN `sop_user_role_mapping` r ON en.`id` = r.`emp_id` WHERE r.`role_id` = 6) AS endorserName, \n" +
            /*"  (SELECT dg3.`designation_name` FROM `t_workflow_details` wke LEFT JOIN `sop_employee` empE ON wke.`action_taken_by` = empE.`id` LEFT JOIN `sop_designation` dg3 ON empE.`designation` = dg3.`id` WHERE wke.`sop_file_id` = sop.`id` AND wke.`is_endorsed` = 8 ORDER BY wke.`action_taken_on` ASC LIMIT 1) AS endorserDesignation,\n" +*/
            "  (SELECT d.`designation_name` FROM `sop_designation` d LEFT JOIN `sop_employee` e ON d.`id` = e.`designation` LEFT JOIN `sop_user_role_mapping` r ON e.`id` = r.`emp_id` WHERE r.`role_id` = 6) AS endorserDesignation,\n" +
            "  (SELECT DATE_FORMAT(ch.`effective_date`,'%D %b, %Y at %l:%i %p') FROM `t_workflow_details` wk3 LEFT JOIN `sop_history_dtls` ch ON sop.`id` = ch.`sop_no` WHERE wk3.`sop_file_id` = sop.`id` AND wk3.`is_endorsed` = 8 AND ch.`sop_type` = 1 GROUP BY ch.`sop_type` LIMIT 1) AS endorsedOn \n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  LEFT JOIN `sop_employee` au\n" +
            "    ON sop.`created_by` = au.`id`\n" +
            "    LEFT JOIN `sop_designation` dg\n" +
            "    ON au.`designation` = dg.`id`\n" +
            "  LEFT JOIN `sop_department` dep\n" +
            "    ON dep.`id` = sop.`dept_id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE sop.`id` = ?1", nativeQuery = true)
    ViewSOPResponsibilityInterface getSopTitleDetails(Integer sopId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId, \n" +
            "  a.`responsibility_name` AS responsibilityName, \n" +
            "  rl.`designation_name` AS roleHolderName, \n" +
            "  DATE_FORMAT(a.`effective_date`, '%D %b, %Y at %l:%i %p') AS effectiveDate, \n" +
            "  GROUP_CONCAT(DISTINCT(b.`sec_id`)) AS secList, \n" +
            "  GROUP_CONCAT(DISTINCT c.department_short_code ORDER BY c.department_short_code ASC SEPARATOR ', ') AS departmentName, \n" +
            "  GROUP_CONCAT(DISTINCT(c.`department_short_code`)) AS deptName \n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "    ON a.`res_id` = b.`resp_id`\n" +
            "  LEFT JOIN `sop_department` c\n" +
            "    ON b.`dept_id` = c.`id`\n" +
            "  LEFT JOIN `t_workflow_details` e\n" +
            "    ON e.`sop_file_id` = a.`sop_id`\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = a.`role_holder`\n" +
            "WHERE a.`res_id` = ?1 AND a.`is_endorsed` = 'Y'\n" +
            "GROUP BY a.`res_id`", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> viewResponsibilitiesLstByViewer(Integer respId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId,\n" +
            "  a.`responsibility_name` AS responsibilityName,\n" +
            "  rl.`designation_name` AS roleHolderName,\n" +
            "  GROUP_CONCAT(DISTINCT(b.`sec_id`)) AS secList, \n" +
            "  a.`want_to_delete` AS respDelete, \n" +
            "  GROUP_CONCAT(DISTINCT(c.`department_short_code`)) AS deptName  \n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "    ON a.`res_id` = b.`resp_id`\n" +
            "  LEFT JOIN `sop_department` c\n" +
            "    ON b.`dept_id` = c.`id`\n" +
            "  LEFT JOIN `t_workflow_details` e\n" +
            "    ON e.`sop_file_id` = a.`sop_id`\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = a.`role_holder`\n" +
            "WHERE a.`sop_id` = ?1\n" +
            "  AND a.`type_id` = ?2\n" +
            "GROUP BY a.`res_id`", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> viewResponsibilitiesLstByVerifier(Integer sopFileId, Integer typeId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId\n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "WHERE a.`sop_id` = ?1", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> getRespIdList(Integer sopId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId\n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "WHERE a.`sop_id` = ?1 AND a.`is_endorsed` = 'Y' AND a.`is_addendum` = 'N'", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> getEndorsedRespList(Integer sopId);


    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId,\n" +
            "  a.`responsibility_name` AS responsibilityName,\n" +
            "  rl.`designation_name` AS roleHolderName,\n" +
            "  a.`want_to_delete` AS respDelete, \n" +
            "  DATE_FORMAT(a.`effective_date`, '%D %b, %Y at %l:%i %p') AS effectiveDate, \n" +
            "  GROUP_CONCAT(DISTINCT c.department_short_code ORDER BY c.department_short_code ASC SEPARATOR ', ') AS departmentName, \n" +
            "  GROUP_CONCAT(DISTINCT(c.`department_short_code`)) AS deptName  \n" +
            "FROM\n" +
            "  `sop_responsibilities` a\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "    ON a.`res_id` = b.`resp_id`\n" +
            "  LEFT JOIN `sop_department` c\n" +
            "    ON b.`dept_id` = c.`id`\n" +
            "  LEFT JOIN `t_workflow_details` e\n" +
            "    ON e.`sop_file_id` = a.`sop_id`\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = a.`role_holder`\n" +
            "  LEFT JOIN `sop_title` s\n" +
            "    ON a.`sop_id` = s.`id`\n" +
            "WHERE a.`res_id` = ?1\n" +
            "  AND a.`is_endorsed` = 'N'\n" +
            "  AND a.`is_addendum` = 'Y'\n" +
            "  AND s.`update_type` = a.`type_id`\n" +
            "GROUP BY a.`res_id`", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> getOngoingAddendumResp(Integer respId);

    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId,\n" +
            "  a.`responsibility_name` AS responsibilityName,\n" +
            "  rl.`id` AS roleHolder,\n" +
            "  rl.`designation_name` AS roleHolderName,\n" +
            "  a.`content` AS content,\n" +
            "  a.`type_id` AS sopTypeId,\n" +
            "  a.`remarks` AS remarks,\n" +
            "  (SELECT\n" +
            "    GROUP_CONCAT(DISTINCT(b.`sec_id`))\n" +
            "  FROM\n" +
            "    `sop_responsibilities_audit` su\n" +
            "    LEFT JOIN `sop_resp_sec_mapping` b\n" +
            "      ON su.`res_id` = b.`resp_id`\n" +
            "  WHERE su.`res_id` = a.`res_id`\n" +
            "  ORDER BY su.`id` DESC\n" +
            "  LIMIT 1) AS secList,\n" +
            "  (SELECT\n" +
            "    GROUP_CONCAT(\n" +
            "      DISTINCT(ad.`department_short_code`)\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_responsibilities_audit` aud\n" +
            "    LEFT JOIN `sop_resp_sec_mapping` rsp\n" +
            "      ON aud.`res_id` = rsp.`resp_id`\n" +
            "    LEFT JOIN `sop_department` ad\n" +
            "      ON rsp.`dept_id` = ad.`id`\n" +
            "  WHERE aud.`res_id` = a.`res_id`\n" +
            "  ORDER BY aud.`id` DESC\n" +
            "  LIMIT 1) AS deptName\n" +
            "FROM\n" +
            "  `sop_responsibilities_audit` a\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = a.`role_holder`\n" +
            "WHERE a.`res_id` = ?1\n" +
            "ORDER BY a.`id` DESC\n" +
            "LIMIT 1", nativeQuery = true)
    List<ResponsibilityInterfaceDTO> viewOldResponsibilitiesLstDtlsViewer(Integer respId);


    @Query(value = "SELECT\n" +
            "  a.`res_id` AS respId,\n" +
            "  a.`responsibility_name` AS responsibilityName,\n" +
            "  a.`content` AS content\n" +
            "FROM\n" +
            "  `sop_responsibilities_audit` a\n" +
            "WHERE a.`res_id` = ?1\n" +
            "ORDER BY a.`id` DESC\n" +
            "LIMIT 1", nativeQuery = true)
    ResponsibilityInterfaceDTO viewOldResponsibilityContent(Integer respId);


    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_inter_related_resp`\n" +
            "WHERE `resp_related_from` = ?1",nativeQuery = true)
    void deleteFromRelated(Integer respId);

    @Transactional
    @Modifying
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_inter_related_resp`\n" +
            "WHERE `sec_related_to` = ?1\n" +
            "  AND `resp_related_from` = ?2", nativeQuery = true)
    void deleteExistingRelatedSecId(String secId, Integer respId);

    /*@Query(value = "SELECT\n" +
            "  dep.`department_name` AS departmentName,\n" +
            "  sec.`section_name` AS sopTitle,\n" +
            "  (SELECT CONCAT('Addendum ',a.`current_sop_no`) FROM `sop_history_dtls` a WHERE a.`sop_no` = sop.`id` AND a.`is_endorsed` = 'Y' ORDER BY a.`effective_date` DESC LIMIT 1) AS sopNumber,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(au.`first_name`),' ',IFNULL(au.`middle_name`, ''))),' ',LTRIM(au.`last_name`)) AS authorName,\n" +
            "  dg.`designation_name` AS authorDesignation,\n" +
            "  DATE_FORMAT(sop.`updated_on`, '%D %b, %Y at %l:%i %p') AS createdOn,  \n" +
            "  (SELECT CONCAT(RTRIM(CONCAT(RTRIM(re.`first_name`),' ',IFNULL(re.`middle_name`, ''))),' ',LTRIM(re.`last_name`)) FROM `t_workflow_details_audit` wk1 LEFT JOIN `sop_employee` re ON wk1.`action_taken_by` = re.`id` WHERE wk1.`sop_file_id` = sop.`id` AND wk1.`stage_id` = 4 ORDER BY wk1.`action_taken_on` ASC LIMIT 1) AS reviewerName, \n" +
            "  (SELECT dg2.`designation_name` FROM `t_workflow_details_audit` wka LEFT JOIN `sop_employee` empR ON wka.`action_taken_by` = empR.`id` LEFT JOIN `sop_designation` dg2 ON empR.`designation` = dg2.`id` WHERE wka.`sop_file_id` = sop.`id` AND wka.`stage_id` = 4 ORDER BY wka.`action_taken_on` ASC LIMIT 1) AS reviewerDesignation,\n" +
            "  (SELECT DATE_FORMAT(wk2.`action_taken_on`, '%D %b, %Y at %l:%i %p') FROM `t_workflow_details_audit` wk2 WHERE wk2.`sop_file_id` = sop.`id` AND wk2.`stage_id` IN (4) ORDER BY wk2.`action_taken_on` DESC LIMIT 1) AS reviewedOn, \n" +
            "  (SELECT CONCAT(RTRIM(CONCAT(RTRIM(en.`first_name`),' ',IFNULL(en.`middle_name`, ''))),' ',LTRIM(en.`last_name`))FROM `sop_employee` en LEFT JOIN `sop_user_role_mapping` r ON en.`id` = r.`emp_id` WHERE r.`role_id` = 6) AS endorserName, \n" +
            "  (SELECT d.`designation_name` FROM `sop_designation` d LEFT JOIN `sop_employee` e ON d.`id` = e.`designation` LEFT JOIN `sop_user_role_mapping` r ON e.`id` = r.`emp_id` WHERE r.`role_id` = 6) AS endorserDesignation,\n" +
            "  (SELECT DATE_FORMAT(wk2.`action_taken_on`, '%D %b, %Y at %l:%i %p') FROM `t_workflow_details_audit` wk2 WHERE wk2.`sop_file_id` = sop.`id` AND wk2.`stage_id` IN (8) ORDER BY wk2.`action_taken_on` DESC LIMIT 1) AS endorsedOn \n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  LEFT JOIN `sop_employee` au\n" +
            "    ON sop.`created_by` = au.`id`\n" +
            "    LEFT JOIN `sop_designation` dg\n" +
            "    ON au.`designation` = dg.`id`\n" +
            "  LEFT JOIN `sop_department` dep\n" +
            "    ON dep.`id` = sop.`dept_id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE sop.`id` = ?1", nativeQuery = true)*/

    @Query(value = "SELECT\n" +
            "  dep.`department_name` AS departmentName,\n" +
            "  sec.`section_name` AS sopTitle,\n" +
            "  (SELECT\n" +
            "    CONCAT('Addendum ', a.`current_sop_no`)\n" +
            "  FROM\n" +
            "    `sop_history_dtls` a\n" +
            "  WHERE a.`sop_no` = sop.`id`\n" +
            "    AND a.`is_endorsed` = 'Y'\n" +
            "  ORDER BY a.`effective_date` DESC\n" +
            "  LIMIT 1) AS sopNumber,\n" +
            "  (SELECT\n" +
            "    CONCAT (\n" +
            "      RTRIM (\n" +
            "        CONCAT (\n" +
            "          RTRIM (a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL (a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM (a.`last_name`)\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "  WHERE a.`id` = sop.`created_by`) AS authorName,\n" +
            "  (SELECT\n" +
            "    b.`designation_name`\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS authorDesignation,\n" +
            "  (SELECT\n" +
            "    DATE_FORMAT(\n" +
            "      crOn.`action_taken_on`,\n" +
            "      '%D %b, %Y at %l:%i %p'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `t_workflow_details_audit` crOn\n" +
            "  WHERE crOn.`sop_file_id` = sop.`id`\n" +
            "    AND crOn.`stage_id` IN (2)\n" +
            "  ORDER BY crOn.`action_taken_on` DESC\n" +
            "  LIMIT 1) AS createdOn,\n" +
            "  (SELECT\n" +
            "    CONCAT (\n" +
            "      RTRIM (\n" +
            "        CONCAT (\n" +
            "          RTRIM (re.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL (re.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM (re.`last_name`)\n" +
            "    )\n" +
            "  FROM\n" +
            "    `t_workflow_details_audit` wk1\n" +
            "    LEFT JOIN `sop_employee` re\n" +
            "      ON wk1.`action_taken_by` = re.`id`\n" +
            "  WHERE wk1.`sop_file_id` = sop.`id`\n" +
            "    AND wk1.`stage_id` = 4\n" +
            "  ORDER BY wk1.`action_taken_on` ASC\n" +
            "  LIMIT 1) AS reviewerName,\n" +
            "  (SELECT\n" +
            "    dg2.`designation_name`\n" +
            "  FROM\n" +
            "    `t_workflow_details_audit` wka\n" +
            "    LEFT JOIN `sop_employee` empR\n" +
            "      ON wka.`action_taken_by` = empR.`id`\n" +
            "    LEFT JOIN `sop_designation` dg2\n" +
            "      ON empR.`designation` = dg2.`id`\n" +
            "  WHERE wka.`sop_file_id` = sop.`id`\n" +
            "    AND wka.`stage_id` = 4\n" +
            "  ORDER BY wka.`action_taken_on` ASC\n" +
            "  LIMIT 1) AS reviewerDesignation,\n" +
            "  (SELECT\n" +
            "    DATE_FORMAT(\n" +
            "      wk2.`action_taken_on`,\n" +
            "      '%D %b, %Y at %l:%i %p'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `t_workflow_details_audit` wk2\n" +
            "  WHERE wk2.`sop_file_id` = sop.`id`\n" +
            "    AND wk2.`stage_id` IN (4)\n" +
            "  ORDER BY wk2.`action_taken_on` DESC\n" +
            "  LIMIT 1) AS reviewedOn,\n" +
            "  (SELECT\n" +
            "    CONCAT (\n" +
            "      RTRIM (\n" +
            "        CONCAT (\n" +
            "          RTRIM (en.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL (en.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM (en.`last_name`)\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` en\n" +
            "    LEFT JOIN `sop_user_role_mapping` r\n" +
            "      ON en.`id` = r.`emp_id`\n" +
            "  WHERE r.`role_id` = 6) AS endorserName,\n" +
            "  (SELECT\n" +
            "    d.`designation_name`\n" +
            "  FROM\n" +
            "    `sop_designation` d\n" +
            "    LEFT JOIN `sop_employee` e\n" +
            "      ON d.`id` = e.`designation`\n" +
            "    LEFT JOIN `sop_user_role_mapping` r\n" +
            "      ON e.`id` = r.`emp_id`\n" +
            "  WHERE r.`role_id` = 6) AS endorserDesignation,\n" +
            "  (SELECT\n" +
            "    DATE_FORMAT(\n" +
            "      wk2.`action_taken_on`,\n" +
            "      '%D %b, %Y at %l:%i %p'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `t_workflow_details` wk2\n" +
            "  WHERE wk2.`sop_file_id` = sop.`id`\n" +
            "    AND wk2.`stage_id` IN (8)\n" +
            "  ORDER BY wk2.`action_taken_on` DESC\n" +
            "  LIMIT 1) AS endorsedOn\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  LEFT JOIN `sop_department` dep\n" +
            "    ON dep.`id` = sop.`dept_id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE sop.`id` = ?1", nativeQuery = true)
    ViewSOPResponsibilityInterface getSopTitleDetailSFromAudit(Integer sopId);


    @Query(value = "SELECT\n" +
            "  a.`remarks` AS remarks,\n" +
            "  a.`status` AS STATUS,\n" +
            "  b.`section_name` AS secName\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping` a\n" +
            "  LEFT JOIN `sop_section` b\n" +
            "  ON a.`sec_id` = b.`id`\n" +
            "WHERE a.`resp_id` = ?1\n" +
            "AND a.`status` = 'D'", nativeQuery = true)
    List<RemarkInterface>  getRemarks(Integer respId);
}
