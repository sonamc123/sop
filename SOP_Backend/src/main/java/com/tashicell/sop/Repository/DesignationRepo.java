package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.DesignationMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignationRepo extends JpaRepository<DesignationMaster, Integer> {
}
