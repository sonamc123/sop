package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.DepartmentMaster;
import com.tashicell.sop.Record.DepartmentMasterInterface;
import com.tashicell.sop.Record.StageMasterInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepo extends JpaRepository<DepartmentMaster, Integer> {
    
	@Query(value = "SELECT\n" +
            "  a.`id` AS deptID,\n" +
            "  a.`department_name` AS deptName\n" +
            "FROM\n" +
            "  `sop_department` a\n" +
            "WHERE a.`id` NOT IN (12)\n" +
            "  AND a.`status` = 1", nativeQuery = true)
    List<DepartmentMasterInterface> getDepartmentMaster();



    @Query(value = "SELECT\n" +
            "  a.`id` AS stageID,\n" +
            "  a.`stage_name` AS stageName\n" +
            "FROM\n" +
            "  `sop_stage` a WHERE a.`id` in (5, 7, 9, 11, 13, 14)", nativeQuery = true)
    List<StageMasterInterface> fetchStageMaster();
}
