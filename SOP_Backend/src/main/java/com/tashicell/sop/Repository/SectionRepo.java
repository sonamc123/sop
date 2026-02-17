package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.SectionMaster;
import com.tashicell.sop.Record.RoleHolderMasterInterface;
import com.tashicell.sop.Record.SectionInterfaceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SectionRepo extends JpaRepository<SectionMaster, Integer> {
    @Query(value = "SELECT\n" +
            "  a.`id` AS secId,\n" +
            "  a.`section_name` AS secName\n" +
            "FROM\n" +
            "  `sop_section` a\n" +
            "WHERE a.`isSection` = 'Y'\n" +
            "AND a.`status` = 1", nativeQuery = true)
    List<SectionInterfaceDTO> getSectionMaster();


    @Query(value = "SELECT\n" +
            "  a.`id` AS secId,\n" +
            "  a.`section_name` AS secName\n" +
            "FROM\n" +
            "  `sop_section` a\n" +
            "WHERE a.`department_id_id` = ?1", nativeQuery = true)
    List<SectionInterfaceDTO> getSectionByDeptId(Integer deptId);


    @Query(value = "SELECT\n" +
            "  b.`responsibility_name` AS roleHolderName,\n" +
            "  b.`res_id` AS roleHolderId\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping` a\n" +
            "  LEFT JOIN `sop_responsibilities` b\n" +
            "  ON a.`resp_id` = b.`res_id`\n" +
            "WHERE a.`sec_id` = ?1", nativeQuery = true)
    List<RoleHolderMasterInterface> getResponsibilityDtls(Integer secId);
}
