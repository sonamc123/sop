package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.RoleMaster;
import com.tashicell.sop.Record.RoleHolderMasterInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleRepo extends JpaRepository<RoleMaster, Integer>{

    @Query(value = "SELECT\n" +
            "  a.`id` AS roleHolderId,\n" +
            "  a.`role_name` AS roleHolderName\n" +
            "FROM\n" +
            "  `sop_role_master` a", nativeQuery = true)
    List<RoleHolderMasterInterface> getRoleMaster();
}
