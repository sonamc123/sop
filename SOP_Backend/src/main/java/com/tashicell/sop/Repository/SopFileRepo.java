package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.SectionMaster;
import com.tashicell.sop.Modal.SopFileDetails;
import com.tashicell.sop.Record.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;

public interface SopFileRepo extends JpaRepository<SopFileDetails, Integer> {
    @Query(value = "SELECT\n" +
            "  sop.`id` AS sopId,\n" +
            "  st.`stage_name` AS stageName,\n" +
            "  sop.`sop_version` AS sopVersion,\n" +
            "  dept.`department_name` AS deptName,\n" +
            "  sop.`introduction` AS introduction,\n" +
            "  sec.`section_name` AS secName,\n" +
            "  wf.`remarks` AS remarks,\n" +
            "  wf.`is_endorsed` AS isEndorsed,\n" +
            "  DATE_FORMAT(\n" +
            "    sop.`created_on`,\n" +
            "    '%D %b, %Y at %l:%i %p'\n" +
            "  ) AS createdOn,\n" +
            "  (SELECT\n" +
            "    CONCAT(\n" +
            "      RTRIM(\n" +
            "        CONCAT(\n" +
            "          RTRIM(a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL(a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM(a.`last_name`),\n" +
            "      ' (',\n" +
            "      b.`designation_name`,\n" +
            "      ', ',\n" +
            "      c.`department_short_code`,\n" +
            "      ')'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "    INNER JOIN `sop_department` c\n" +
            "      ON a.`department_id` = c.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS createdBy\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  INNER JOIN `t_workflow_details` wf\n" +
            "    ON sop.`id` = wf.`sop_file_id`\n" +
            "  LEFT JOIN `sop_stage` st\n" +
            "    ON wf.`stage_id` = st.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON sop.`dept_id` = dept.`id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE sop.`sec_id` = ?1\n" +
            "  AND wf.stage_id NOT IN(8)\n" +
            "ORDER BY wf.`action_taken_on` ASC", nativeQuery = true)
    List<ViewSopDetailsInterfaceDTO> findSopDtlsBySectionId(Integer sectionId);


    @Query(value = "SELECT\n" +
            "  sop.`id` AS sopId,\n" +
            "  st.`stage_name` AS stageName,\n" +
            "  sop.`sop_version` AS sopVersion,\n" +
            "  dept.`department_name` AS deptName,\n" +
            "  sop.`introduction` AS introduction,\n" +
            "  sec.`section_name` AS secName,\n" +
            "  wf.`remarks` AS remarks,\n" +
            "  DATE_FORMAT(\n" +
            "    sop.`created_on`,\n" +
            "    '%D %b, %Y at %l:%i %p'\n" +
            "  ) AS createdOn,\n" +
            "  (SELECT\n" +
            "    CONCAT(\n" +
            "      RTRIM(\n" +
            "        CONCAT(\n" +
            "          RTRIM(a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL(a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM(a.`last_name`),\n" +
            "      ' (',\n" +
            "      b.`designation_name`,\n" +
            "      ', ',\n" +
            "      c.`department_short_code`,\n" +
            "      ')'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "    INNER JOIN `sop_department` c\n" +
            "      ON a.`department_id` = c.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS createdBy\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  INNER JOIN `t_workflow_details` wf\n" +
            "    ON sop.`id` = wf.`sop_file_id`\n" +
            "  LEFT JOIN `sop_stage` st\n" +
            "    ON wf.`stage_id` = st.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON sop.`dept_id` = dept.`id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE sop.`dept_id` = ?1\n" +
            "  AND wf.stage_id IN (2, 7)", nativeQuery = true)
    List<ViewSopDetailsInterfaceDTO> findSopDtlsByDeptId(Integer deptId);

    @Query(value = "SELECT\n" +
            "  sop.`id` AS sopId,\n" +
            "  st.`stage_name` AS stageName,\n" +
            "  sop.`sop_version` AS sopVersion,\n" +
            "  dept.`department_name` AS deptName,\n" +
            "  sop.`introduction` AS introduction,\n" +
            "  sec.`section_name` AS secName,\n" +
            "  DATE_FORMAT(\n" +
            "    sop.`created_on`,\n" +
            "    '%D %b, %Y at %l:%i %p'\n" +
            "  ) AS createdOn,\n" +
            "  (SELECT\n" +
            "    CONCAT(\n" +
            "      RTRIM(\n" +
            "        CONCAT(\n" +
            "          RTRIM(a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL(a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM(a.`last_name`),\n" +
            "      ' (',\n" +
            "      b.`designation_name`,\n" +
            "      ', ',\n" +
            "      c.`department_short_code`,\n" +
            "      ')'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "    INNER JOIN `sop_department` c\n" +
            "      ON a.`department_id` = c.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS createdBy\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  LEFT JOIN `sop_endorser_task_list` en\n" +
            "    ON sop.`id` = en.`sop_file_id`\n" +
            "  LEFT JOIN `sop_stage` st\n" +
            "    ON en.`stage_id` = st.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON sop.`dept_id` = dept.`id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE en.`endorser_id` = ?1\n" +
            "  AND en.`stage_id` = ?2", nativeQuery = true)
    List<ViewSopDetailsInterfaceDTO> findSopDtlsByEndorser(Integer userId, Integer stageId);

    @Query(value = "SELECT\n" +
            "  IFNULL(a.`remarks`, \"No Action Taken\") AS actionRemarks, \n" +
            "  IFNULL(DATE_FORMAT(a.`action_taken_on`, '%D %b, %Y at %l:%i %p'), \"No Action Taken\") AS actionTakenOn, \n" +
            "  b.`stage_name` AS actionStatus, \n" +
            "  IFNULL(CONCAT(RTRIM(CONCAT(RTRIM(emp.`first_name`),' ',IFNULL(emp.`middle_name`, ''))),' ',LTRIM(emp.`last_name`), ' ','(',desg.`designation_name`, ',', ' ', dept.`department_name`,')'), \"No Action Taken\") AS actionTakenBy \n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "  LEFT JOIN `sop_stage` b\n" +
            "    ON a.`stage_id` = b.`id`\n" +
            "  LEFT JOIN `sop_employee` emp\n" +
            "    ON a.`endorser_id` = emp.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON emp.`department_id` = dept.`id`\n" +
            "  LEFT JOIN `sop_designation` desg\n" +
            "    ON emp.`designation` = desg.`id`\n" +
            "WHERE a.`sop_file_id` = ?1", nativeQuery = true)
    List<EndorserActionTypeInterface> getEndorserActionList(Integer sopFileId);


    @Query(value = "SELECT\n" +
            "  sop.id AS sopId,\n" +
            "  st.stage_name AS stageName,\n" +
            "  sop.sop_version AS sopVersion,\n" +
            "  dept.department_name AS deptName,\n" +
            "  sop.introduction AS introduction,\n" +
            "  sec.section_name AS secName,\n" +
            "  wf.remarks AS remarks,\n" +
            "  DATE_FORMAT(\n" +
            "    sop.created_on,\n" +
            "    '%D %b, %Y at %l:%i %p'\n" +
            "  ) AS createdOn,\n" +
            "  (SELECT\n" +
            "    CONCAT(\n" +
            "      RTRIM(\n" +
            "        CONCAT(\n" +
            "          RTRIM(a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL(a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM(a.`last_name`),\n" +
            "      ' (',\n" +
            "      b.`designation_name`,\n" +
            "      ', ',\n" +
            "      c.`department_short_code`,\n" +
            "      ')'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "    INNER JOIN `sop_department` c\n" +
            "      ON a.`department_id` = c.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS createdBy\n" +
            "FROM\n" +
            "  sop_title sop\n" +
            "  INNER JOIN t_workflow_details wf\n" +
            "    ON sop.id = wf.sop_file_id\n" +
            "  LEFT JOIN sop_stage st\n" +
            "    ON wf.is_endorsed = st.id\n" +
            "  INNER JOIN sop_department dept\n" +
            "    ON sop.dept_id = dept.id\n" +
            "  LEFT JOIN sop_section sec\n" +
            "    ON sop.sec_id = sec.id\n" +
            "WHERE sop.dept_id = ?1\n" +
            "  AND sec.status = 1\n" +
            "  AND wf.is_endorsed = 8", nativeQuery = true)
    List<ViewSopDetailsInterfaceDTO> getViewerTaskList(Integer deptId);

    @Query(value = "SELECT\n" +
            "  sop.`id` AS sopId,\n" +
            "  st.`stage_name` AS stageName,\n" +
            "  v.`current_sop_no` AS sopVersion,\n" +
            "  dept.`department_name` AS deptName,\n" +
            "  sop.`introduction` AS introduction,\n" +
            "  sec.`section_name` AS secName,\n" +
            "  wf.`remarks` AS remarks,\n" +
            "  DATE_FORMAT(sop.`created_on`, '%D %b, %Y at %l:%i %p') AS createdOn, \n" +
            "  (SELECT\n" +
            "    CONCAT(\n" +
            "      RTRIM(\n" +
            "        CONCAT(\n" +
            "          RTRIM(a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL(a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM(a.`last_name`),\n" +
            "      ' (',\n" +
            "      b.`designation_name`,\n" +
            "      ', ',\n" +
            "      c.`department_short_code`,\n" +
            "      ')'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "    INNER JOIN `sop_department` c\n" +
            "      ON a.`department_id` = c.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS createdBy\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  INNER JOIN `t_workflow_details` wf\n" +
            "    ON sop.`id` = wf.`sop_file_id`\n" +
            "  LEFT JOIN `sop_stage` st\n" +
            "    ON wf.`stage_id` = st.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON sop.dept_id  = dept.`id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "  LEFT JOIN `sop_history_dtls` v\n" +
            "    ON sop.`id` = v.`sop_no`\n" +
            "WHERE sop.`sec_id` = ?1\n" +
            "  AND wf.is_endorsed = 8\n" +
            "  AND v.`is_endorsed` = 'Y'\n" +
            "ORDER BY v.`effective_date` DESC\n" +
            "LIMIT 1", nativeQuery = true)
    ViewSopDetailsInterfaceDTO getEndorsedSOP(Integer secId);

    @Query(value = "SELECT\n" +
            "  sop.`introduction` AS introduction\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "WHERE sop.`id` = ?1", nativeQuery = true)
    ViewSopDetailsInterfaceDTO getSopDesc(Integer sopId);


    @Query(value = "SELECT\n" +
            "  b.`stage_name` AS actionStatus,\n" +
            "  a.`remarks` AS remark,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(c.`first_name`),' ',IFNULL(c.`middle_name`, ''))),' ',LTRIM(c.`last_name`)) AS endorserName,\n" +
            "  d.`department_short_code` AS endorserDept,\n" +
            "  DATE_FORMAT(a.`action_taken_on`, '%D %b, %Y at %l:%i %p') AS actionTakenOn\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "  LEFT JOIN `sop_stage` b\n" +
            "  ON a.`stage_id` = b.`id`\n" +
            "  LEFT JOIN `sop_employee` c\n" +
            "  ON a.`endorser_id` = c.`id`\n" +
            "  LEFT JOIN `sop_department` d\n" +
            "  ON c.`department_id` = d.`id`\n" +
            "WHERE a.`sop_file_id` = ?1 AND a.`stage_id` = 6", nativeQuery = true)
    List<EndorserRemarkInterface> getEndorserRemark(Integer sopFileId);

    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_endorser_task_list`\n" +
            "WHERE `sop_file_id` = ?1 AND `stage_id` = 6", nativeQuery = true)
    void deleteEndorserTasklistOnAddendum(Integer sopId);


    @Query(value = "SELECT\n" +
            "  sop.`id` AS sopId,\n" +
            "  st.`stage_name` AS stageName,\n" +
            "  sop.`sop_version` AS sopVersion,\n" +
            "  dept.`department_name` AS deptName,\n" +
            "  sop.`introduction` AS introduction,\n" +
            "  sec.`section_name` AS secName,\n" +
            "  IFNULL(\n" +
            "    sop.`addendum_reason`,\n" +
            "    wf.`remarks`\n" +
            "  ) AS remarks,\n" +
            "  DATE_FORMAT(\n" +
            "    sop.`created_on`,\n" +
            "    '%D %b, %Y at %l:%i %p'\n" +
            "  ) AS createdOn,\n" +
            "  (SELECT\n" +
            "    CONCAT(\n" +
            "      RTRIM(\n" +
            "        CONCAT(\n" +
            "          RTRIM(a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL(a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM(a.`last_name`),\n" +
            "      ' (',\n" +
            "      b.`designation_name`,\n" +
            "      ', ',\n" +
            "      c.`department_short_code`,\n" +
            "      ')'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "    INNER JOIN `sop_department` c\n" +
            "      ON a.`department_id` = c.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS createdBy\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  INNER JOIN `t_workflow_details` wf\n" +
            "    ON sop.`id` = wf.`sop_file_id`\n" +
            "  LEFT JOIN `sop_stage` st\n" +
            "    ON wf.`stage_id` = st.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON sop.`dept_id` = dept.`id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE wf.stage_id = ?1", nativeQuery = true)
    List<ViewSopDetailsInterfaceDTO> getFocalTaskList(Integer StageID);


    @Query(value = "SELECT\n" +
            "  sop.`id` AS sopId,\n" +
            "  st.`stage_name` AS stageName,\n" +
            "  sop.`sop_version` AS sopVersion,\n" +
            "  dept.`department_name` AS deptName,\n" +
            "  sop.`introduction` AS introduction,\n" +
            "  sec.`section_name` AS secName,\n" +
            "  IFNULL(\n" +
            "    sop.`addendum_reason`,\n" +
            "    wf.`remarks`\n" +
            "  ) AS remarks,\n" +
            "  DATE_FORMAT(\n" +
            "    sop.`created_on`,\n" +
            "    '%D %b, %Y at %l:%i %p'\n" +
            "  ) AS createdOn,\n" +
            "  (SELECT\n" +
            "    CONCAT(\n" +
            "      RTRIM(\n" +
            "        CONCAT(\n" +
            "          RTRIM(a.`first_name`),\n" +
            "          ' ',\n" +
            "          IFNULL(a.`middle_name`, '')\n" +
            "        )\n" +
            "      ),\n" +
            "      ' ',\n" +
            "      LTRIM(a.`last_name`),\n" +
            "      ' (',\n" +
            "      b.`designation_name`,\n" +
            "      ', ',\n" +
            "      c.`department_short_code`,\n" +
            "      ')'\n" +
            "    )\n" +
            "  FROM\n" +
            "    `sop_employee` a\n" +
            "    INNER JOIN `sop_designation` b\n" +
            "      ON a.`designation` = b.`id`\n" +
            "    INNER JOIN `sop_department` c\n" +
            "      ON a.`department_id` = c.`id`\n" +
            "  WHERE a.`id` = sop.`created_by`) AS createdBy\n" +
            "FROM\n" +
            "  `sop_title` sop\n" +
            "  INNER JOIN `t_workflow_details` wf\n" +
            "    ON sop.`id` = wf.`sop_file_id`\n" +
            "  LEFT JOIN `sop_stage` st\n" +
            "    ON wf.`stage_id` = st.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON sop.`dept_id` = dept.`id`\n" +
            "  LEFT JOIN `sop_section` sec\n" +
            "    ON sop.`sec_id` = sec.`id`\n" +
            "WHERE wf.stage_id = ?1", nativeQuery = true)
    List<ViewSopDetailsInterfaceDTO> getAuthorizerTaskList(Integer endorserStage);

    @Query(value = "SELECT\n" +
            "  a.`id`\n" +
            "FROM\n" +
            "  `sop_title` a\n" +
            "  LEFT JOIN `t_workflow_details` b\n" +
            "  ON a.`id` = b.`sop_file_id`\n" +
            "WHERE a.`sec_id` = ?1 AND b.`is_endorsed` = 8 AND NOT a.`update_type` = 2", nativeQuery = true)
    BigInteger checkIfExist(Integer secId);

    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `t_workflow_details`\n" +
            "WHERE `sop_file_id` = ?1", nativeQuery = true)
    void deleteWkFlowDtls(Integer sopId);

    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_responsibilities`\n" +
            "WHERE `sop_id` = ?1", nativeQuery = true)
    void deleteRespDtls(Integer sopId);

    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_endorser_task_list`\n" +
            "WHERE `sop_file_id` = ?1", nativeQuery = true)
    void deleteEndorserTasklistDtls(Integer sopId);


    @Query(value = "SELECT\n" +
            "  COUNT(*)\n" +
            "FROM\n" +
            "  `sop_title` a\n" +
            "  LEFT JOIN `t_workflow_details` b\n" +
            "    ON a.`id` = b.`sop_file_id`\n" +
            "WHERE a.`sec_id` = ?1\n" +
            "  AND b.`stage_id` NOT IN(8)", nativeQuery = true)
    BigInteger checkDuplicateSOP(Integer sectionId);


    @Query(value = "SELECT\n" +
            "  res.`res_id` AS respId,\n" +
            "  res.`responsibility_name` AS responsibilityName,\n" +
            "  rl.`designation_name` AS roleHolderName,\n" +
            "  res.`content` AS content,\n" +
            "  sc.`section_name` AS sopSection,\n" +
            "  dp.`department_name` AS sopDpt,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(emp.`first_name`),' ',IFNULL(emp.`middle_name`, ''))),' ',LTRIM(emp.`last_name`)) AS sopCreator,\n" +
            "  DATE_FORMAT(res.`updated_on`, '%D %b, %Y at %l:%i %p') AS createdOn,\n" +
            "  CASE\n" +
            "    WHEN rsm.`status` = 'P'\n" +
            "    THEN 'Pending'\n" +
            "    WHEN rsm.`status` = 'A'\n" +
            "    THEN 'Agreed'\n" +
            "    WHEN rsm.`status` = 'D'\n" +
            "    THEN 'Disagreed'\n" +
            "  END AS roleHolderName\n" +
            "FROM\n" +
            "  `sop_responsibilities` res\n" +
            "  LEFT JOIN `sop_title` sop\n" +
            "    ON res.`sop_id` = sop.`id`\n" +
            "  LEFT JOIN `sop_employee` emp\n" +
            "    ON sop.`created_by` = emp.`id`\n" +
            "  LEFT JOIN `sop_section` sc\n" +
            "    ON sop.`sec_id` = sc.`id`\n" +
            "  LEFT JOIN `sop_department` dp\n" +
            "    ON sop.`dept_id` = dp.`id`\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = res.`role_holder`\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` rsm\n" +
            "    ON res.`res_id` = rsm.`resp_id`\n" +
            "WHERE rsm.`sec_id` = ?1\n" +
            "ORDER BY rsm.`status` DESC", nativeQuery = true)
    List<RelatedDeptSOPInterface> getRelatedResponsibility(Integer secID);


    @Query(value = "SELECT\n" +
            "  res.`res_id` AS respId,\n" +
            "  res.`responsibility_name` AS responsibilityName,\n" +
            "  rl.`designation_name` AS roleHolderName,\n" +
            "  res.`content` AS content,\n" +
            "  sc.`section_name` AS sopSection,\n" +
            "  dp.`department_name` AS sopDpt,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(emp.`first_name`),' ',IFNULL(emp.`middle_name`, ''))),' ',LTRIM(emp.`last_name`)) AS sopCreator, \n" +
            "  DATE_FORMAT(res.`updated_on`, '%D %b, %Y at %l:%i %p') AS createdOn, \n" +
            "  CASE\n" +
            "    WHEN rsm.`status` = 'A'\n" +
            "    THEN 'Agreed'\n" +
            "  END AS roleHolderName\n" +
            "FROM\n" +
            "  `sop_responsibilities` res\n" +
            "  LEFT JOIN `sop_title` sop\n" +
            "    ON res.`sop_id` = sop.`id`\n" +
            "  LEFT JOIN `sop_employee` emp\n" +
            "    ON sop.`created_by` = emp.`id`\n" +
            "  LEFT JOIN `sop_section` sc\n" +
            "    ON sop.`sec_id` = sc.`id`\n" +
            "  LEFT JOIN `sop_department` dp\n" +
            "    ON sop.`dept_id` = dp.`id`\n" +
            "  LEFT JOIN `sop_designation` rl\n" +
            "    ON rl.`id` = res.`role_holder`\n" +
            "  LEFT JOIN `sop_resp_sec_mapping` rsm\n" +
            "    ON res.`res_id` = rsm.`resp_id`\n" +
            "WHERE rsm.`sec_id` = ?1\n" +
            "  AND rsm.`status` = 'A'", nativeQuery = true)
    List<RelatedDeptSOPInterface> getRelatedActivities(Integer secID);

    @Query(value = "SELECT\n" +
            "  COUNT(*)\n" +
            "FROM\n" +
            "  `sop_title` a\n" +
            "WHERE a.`sec_id` = ?1", nativeQuery = true)
    Integer getSopCount(Integer secId);
}
