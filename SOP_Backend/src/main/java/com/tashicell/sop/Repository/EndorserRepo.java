package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.EndorserTaskList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.Optional;

public interface EndorserRepo extends JpaRepository<EndorserTaskList, Integer> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE\n" +
            "  `sop_endorser_task_list`\n" +
            "SET\n" +
            "  `stage_id` = ?1,\n" +
            "  `remarks` = ?2,\n" +
            "  `action_taken_on` = NOW()\n" +
            "WHERE `sop_file_id` = ?3 AND `endorser_id` = ?4",nativeQuery = true)

    void updateApprovedStatus(Integer stageId, String remark, Integer sopFileId, Integer useID);

    @Transactional
    @Modifying
    @Query(value = "UPDATE\n" +
            "  `sop_endorser_task_list`\n" +
            "SET\n" +
            "  `stage_id` = ?1,\n" +
            "  `remarks` = ?2,\n" +
            "  `action_taken_on` = NOW()\n" +
            "WHERE `sop_file_id` = ?3",nativeQuery = true)

    void updateRejectStatus(Integer stageId, String remark, Integer sopFileId);


    @Query(value = "SELECT\n" +
            "  COUNT(*)\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "WHERE a.`sop_file_id` = ?1\n" +
            "  AND a.`stage_id` = 9",nativeQuery = true)
    BigInteger getApprovalCount(Integer sopFileId);


    @Query(value = "SELECT\n" +
            "  a.`reviewer_id`\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "WHERE a.`sop_file_id` = ?1 LIMIT 1",nativeQuery = true)
    String getReviewerId(Integer sopFileId);

    @Query(value = "SELECT\n" +
            "  COUNT(*)\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "WHERE a.`sop_file_id` = ?1\n" +
            "  AND a.`endorser_id` = ?2",nativeQuery = true)
    BigInteger getEndorserCount(Integer sopFileId, Integer endorserId);

    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_endorser_task_list` a\n" +
            "WHERE a.`sop_file_id` = ?1\n" +
            "  AND a.`endorser_id` = ?2", nativeQuery = true)
    Optional<EndorserTaskList> findEndorserTaskList(Integer sopFileId, Integer endorserId);


}
