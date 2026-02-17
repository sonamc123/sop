package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.HistoryDetailsEntity;
import com.tashicell.sop.Record.HistoryDetailsInterface;
import com.tashicell.sop.Record.ViewStatusInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChangeHistoryRepo extends JpaRepository<HistoryDetailsEntity, Integer> {

    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_history_dtls` a\n" +
            "WHERE a.`sop_no` = ?1\n" +
            "  AND a.`sop_type` = ?2 ORDER BY a.`previous_sop_no` DESC LIMIT 1", nativeQuery = true)
    List<HistoryDetailsEntity> findChangeHistory(Integer sopId, Integer SopTypeId);

    @Query(value = "SELECT\n" +
            "  DISTINCT a.`previous_sop_no` AS previousSopNo,\n" +
            "  DATE_FORMAT(a.`effective_date`, '%D %b, %Y at %l:%i %p') AS effectiveDate,   \n" +
            "  a.`significant_changes` AS significantChange,\n" +
            "  a.`sop_type` AS sopTypeId,\n" +
            "  a.`current_sop_no` AS currentSopVno,\n" +
            "  a.`is_addendum` AS isAddendum\n" +
            "FROM\n" +
            "  `sop_history_dtls` a\n" +
            "WHERE a.`sop_no` = ?1\n" +
            "  AND (a.`is_endorsed` IS NULL OR a.`is_endorsed` IN ('Y', ?2))\n" +
            "GROUP BY a.`previous_sop_no`", nativeQuery = true)
    List<HistoryDetailsInterface> getChangeHistory(Integer sopId, String sopVersion);

    @Query(value = "SELECT\n" +
            "  a.`current_sop_no`\n" +
            "FROM\n" +
            "  `sop_history_dtls` a\n" +
            "WHERE a.`sop_no` = ?1\n" +
            "  AND a.`is_endorsed` = 'Y'\n" +
            "  ORDER BY a.`effective_date` DESC LIMIT 1", nativeQuery = true)
    String getSopVersion(Integer sopId);


    @Query(value = "SELECT\n" +
            "  d.section_name AS sectionName,\n" +
            "  b.stage_name AS stageName,\n" +
            "  a.remarks AS remarks,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(emp.`first_name`),' ',IFNULL(emp.`middle_name`, ''))),' ',LTRIM(emp.`last_name`)) AS actionTakenBy,\n" +
            "  DATE_FORMAT(a.`action_taken_on`, '%D %b, %Y at %l:%i %p') AS actionTakenOn\n" +
            "FROM\n" +
            "  t_workflow_details a\n" +
            "  LEFT JOIN sop_stage b\n" +
            "    ON a.stage_id = b.id\n" +
            "  LEFT JOIN sop_title c\n" +
            "    ON a.sop_file_id = c.id\n" +
            "  LEFT JOIN sop_section d\n" +
            "    ON c.sec_id = d.id\n" +
            "  LEFT JOIN sop_employee emp\n" +
            "    ON a.action_taken_by = emp.id\n" +
            "WHERE a.stage_id IN (1,2,3,4,5,6,7,8,10,11,12,13)", nativeQuery = true)
    List<ViewStatusInterface> getAllStatusDetails();


    @Query(value = "SELECT\n" +
            "  d.section_name AS sectionName,\n" +
            "  b.stage_name AS stageName,\n" +
            "  a.remarks AS remarks,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(emp.`first_name`),' ',IFNULL(emp.`middle_name`, ''))),' ',LTRIM(emp.`last_name`)) AS actionTakenBy,\n" +
            "  DATE_FORMAT(a.`action_taken_on`, '%D %b, %Y at %l:%i %p') AS actionTakenOn\n" +
            "FROM\n" +
            "  t_workflow_details a\n" +
            "  LEFT JOIN sop_stage b\n" +
            "    ON a.stage_id = b.id\n" +
            "  LEFT JOIN sop_title c\n" +
            "    ON a.sop_file_id = c.id\n" +
            "  LEFT JOIN sop_section d\n" +
            "    ON c.sec_id = d.id\n" +
            "  LEFT JOIN sop_employee emp\n" +
            "    ON a.action_taken_by = emp.id\n" +
            "WHERE a.stage_id = ?1\n" +
            "  AND c.`dept_id` = ?2\n" +
            "GROUP BY d.`section_name`", nativeQuery = true)
    List<ViewStatusInterface> getEndorserStatusList(Integer stageID, Integer deptId);

    @Query(value = "SELECT\n" +
            "  d.section_name AS sectionName,\n" +
            "  b.stage_name AS stageName,\n" +
            "  a.`sop_file_id` AS sopId,\n" +
            "  a.`remarks` AS remarks,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(e.`first_name`),' ',IFNULL(e.`middle_name`, ''))),' ',LTRIM(e.`last_name`)) AS actionTakenBy,\n" +
            "  DATE_FORMAT(a.`action_taken_on`, '%D %b, %Y at %l:%i %p') AS actionTakenOn\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "  LEFT JOIN sop_stage b\n" +
            "    ON a.`stage_id` = b.id\n" +
            "  LEFT JOIN sop_title c\n" +
            "    ON a.`sop_file_id` = c.id\n" +
            "  LEFT JOIN sop_section d\n" +
            "    ON c.sec_id = d.id\n" +
            "  LEFT JOIN sop_employee e\n" +
            "    ON a.`reviewer_id` = e.`username`\n" +
            "WHERE a.`stage_id` = ?1\n" +
            "  AND c.`dept_id` = ?2\n" +
            "GROUP BY d.`section_name`", nativeQuery = true)
    List<ViewStatusInterface> getPendingList(Integer stageID, Integer deptId);


    @Query(value = "SELECT DISTINCT\n" +
            "  CONCAT(\n" +
            "    RTRIM(\n" +
            "      CONCAT(\n" +
            "        RTRIM(emp.`first_name`),\n" +
            "        ' ',\n" +
            "        IFNULL(emp.`middle_name`, '')\n" +
            "      )\n" +
            "    ),\n" +
            "    ' ',\n" +
            "    LTRIM(emp.`last_name`)\n" +
            "  ) AS actionTakenBy\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "  LEFT JOIN sop_employee emp\n" +
            "    ON a.`endorser_id` = emp.id\n" +
            "WHERE a.`stage_id` = 9 AND a.`sop_file_id` = ?1", nativeQuery = true)
    List<ViewStatusInterface> getEndorserPendingList(Integer sopId);
    @Query(value = "SELECT\n" +
            "  d.section_name AS sectionName,\n" +
            "  b.stage_name AS stageName,\n" +
            "  a.`sop_file_id` AS sopId,\n" +
            "  a.`remarks` AS remarks,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(e.`first_name`),' ',IFNULL(e.`middle_name`, ''))),' ',LTRIM(e.`last_name`)) AS actionTakenBy,\n" +
            "  DATE_FORMAT(a.`action_taken_on`, '%D %b, %Y at %l:%i %p') AS actionTakenOn\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "  LEFT JOIN sop_stage b\n" +
            "    ON a.`stage_id` = b.id\n" +
            "  LEFT JOIN sop_title c\n" +
            "    ON a.`sop_file_id` = c.id\n" +
            "  LEFT JOIN sop_section d\n" +
            "    ON c.sec_id = d.id\n" +
            "  LEFT JOIN sop_employee e\n" +
            "    ON a.`reviewer_id` = e.`username`\n" +
            "WHERE a.`stage_id` = ?1\n" +
            "GROUP BY d.`section_name`", nativeQuery = true)
    List<ViewStatusInterface> getAllDeptPendingList(Integer stageID);


    @Query(value = "SELECT\n" +
            "  d.section_name AS sectionName,\n" +
            "  b.stage_name AS stageName,\n" +
            "  a.remarks AS remarks,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(emp.`first_name`),' ',IFNULL(emp.`middle_name`, ''))),' ',LTRIM(emp.`last_name`)) AS actionTakenBy,\n" +
            "  DATE_FORMAT(a.`action_taken_on`, '%D %b, %Y at %l:%i %p') AS actionTakenOn\n" +
            "FROM\n" +
            "  t_workflow_details a\n" +
            "  LEFT JOIN sop_stage b\n" +
            "    ON a.stage_id = b.id\n" +
            "  LEFT JOIN sop_title c\n" +
            "    ON a.sop_file_id = c.id\n" +
            "  LEFT JOIN sop_section d\n" +
            "    ON c.sec_id = d.id\n" +
            "  LEFT JOIN sop_employee emp\n" +
            "    ON a.action_taken_by = emp.id\n" +
            "WHERE a.stage_id = ?1\n" +
            "GROUP BY d.`section_name`", nativeQuery = true)
    List<ViewStatusInterface> getAllStatusList(Integer stageID);
}
