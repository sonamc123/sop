package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.SopTypeMaster;
import com.tashicell.sop.Record.SopTypeRecordInterfaceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SopTypeMasterRepo extends JpaRepository<SopTypeMaster, Integer> {
    @Query(value = "SELECT s.id AS id, s.name AS name FROM sop_type_master s WHERE s.status = 1", nativeQuery = true)
    List<SopTypeRecordInterfaceDTO> getSopTypeDetails();
}
