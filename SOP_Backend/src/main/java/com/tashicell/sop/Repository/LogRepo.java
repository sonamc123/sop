package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.SopLogDetails;
import com.tashicell.sop.Record.ActivityLogInterfaceDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LogRepo extends JpaRepository <SopLogDetails, Integer>{

    @Query(value = "SELECT\n" +
            "  a.`action` AS actionName,\n" +
            "  CONCAT(RTRIM(CONCAT(RTRIM(emp.`first_name`),' ',IFNULL(emp.`middle_name`, ''))),' ',LTRIM(emp.`last_name`),' (',de.`designation_name`, ', ','(',dept.`department_short_code`,')',')') AS actionTakenBy, \n" +
            "  DATE_FORMAT(a.`action_time`, '%D %b, %Y at %l:%i %p') AS actionTakenOn,\n" +
            "  a.`remarks` AS actionRemark\n" +
            "FROM\n" +
            "  `sop_file_log` a\n" +
            "  LEFT JOIN `sop_employee` emp\n" +
            "    ON a.`action_taken_by` = emp.`id`\n" +
            "  LEFT JOIN `sop_designation` de\n" +
            "    ON emp.`designation` = de.`id`\n" +
            "  LEFT JOIN `sop_section` se\n" +
            "    ON emp.`section_id` = se.`id`\n" +
            "  LEFT JOIN `sop_department` dept\n" +
            "    ON emp.`department_id` = dept.`id`\n" +
            "WHERE a.`sop_file_id` = ?1\n" +
            "  AND a.`sop_version` = ?2\n" +
            "GROUP BY a.`action_taken_by`, a.`action`\n" +
            "ORDER BY a.`action_time` DESC", nativeQuery = true)

    List<ActivityLogInterfaceDTO> getViewerActivityLog(Integer sopId, String sopVersion);

    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            " `sop_file_log`\n" +
            "WHERE `sop_file_id` = ?1\n", nativeQuery = true)
    void deleteLog(Integer sopId);
}
