package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.RoleHolderMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleHolderRepo extends JpaRepository<RoleHolderMaster, Integer> {
}
