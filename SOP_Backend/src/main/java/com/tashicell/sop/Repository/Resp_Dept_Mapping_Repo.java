package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.Responsibility_Section_mapping;
import com.tashicell.sop.Record.ViewSopDetailsInterfaceDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Resp_Dept_Mapping_Repo extends JpaRepository<Responsibility_Section_mapping, Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping`\n" +
            "WHERE `sec_id` = ?1\n" +
            "  AND `resp_id` = ?2", nativeQuery = true)
    void deleteExistingSection(String secId, Integer respId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE\n" +
            "  `sop_resp_sec_mapping`\n" +
            "SET\n" +
            "  `status` = 'A'\n" +
            "WHERE `resp_id` = ?1\n" +
            "  AND `sec_id` = ?2", nativeQuery = true)
    void updateRelatedStatus(Integer respId, Integer secId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE\n" +
            "  `sop_resp_sec_mapping`\n" +
            "SET\n" +
            "  `status` = 'D',\n" +
            "  `remarks` = ?1\n" +
            "WHERE `resp_id` = ?2\n" +
            "  AND `sec_id` = ?3", nativeQuery = true)
    void updateRelatedRemarks(String remarks, Integer respId, Integer secId);


    @Query(value = "SELECT\n" +
            "  a.`remarks` AS remarks,\n" +
            "  b.`section_name` AS secName\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping` a\n" +
            "  LEFT JOIN `sop_section` b\n" +
            "    ON a.`sec_id` = b.`id`\n" +
            "WHERE a.`resp_id` = ?1\n" +
            "  AND a.`status` = 'D'", nativeQuery = true)
    List<ViewSopDetailsInterfaceDTO> getRelatedRemarks(Integer respId);

    @Query(value = "SELECT\n" +
            "  a.`status`\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping` a\n" +
            "WHERE a.`resp_id` = ?1\n" +
            "  AND a.`sec_id` = ?2", nativeQuery = true)
    String getRelatedStatus(Integer respId, Integer secId);


    @Query(value = "SELECT\n" +
            "  a.`relid`\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping` a\n" +
            "WHERE a.`resp_id` = ?1\n" +
            "  AND a.`status` = 'D'", nativeQuery = true)
    List<Integer> getStatus(Integer respId);

    @Query(value = "SELECT\n" +
            "  a.`status`\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping` a\n" +
            "WHERE a.`resp_id` = ?1", nativeQuery = true)
    List<String> checkRelatedStatus(Integer respId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE\n" +
            "  `sop_resp_sec_mapping`\n" +
            "SET\n" +
            "  `status` = 'P'\n" +
            "WHERE `relid` = ?1", nativeQuery = true)
    void updatePendingStatus(Integer relid);


    @Query(value = "SELECT\n" +
            "  a.`sec_id`\n" +
            "FROM\n" +
            "  `sop_resp_sec_mapping` a\n" +
            "WHERE a.`resp_id` = ?1", nativeQuery = true)
    String[] getSecList(Integer respId);
}
