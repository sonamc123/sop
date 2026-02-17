package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.StageMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SopStageRepo extends JpaRepository<StageMaster, Integer> {
}
