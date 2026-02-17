package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SopWorkFLowRepo extends JpaRepository<WorkFlowDetails, Integer> {

    @Query(value = "SELECT * FROM `t_workflow_details` a WHERE a.`sop_file_id` = ?", nativeQuery = true)
    Optional<WorkFlowDetails> getWorkFlowDtls(Integer sopFileId);

}
