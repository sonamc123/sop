package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.EndorsedSOPDoc;
import com.tashicell.sop.Record.DocumentInterfaceViewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EndorsedSopDocRepo extends JpaRepository<EndorsedSOPDoc, Integer> {

    @Query(value = "SELECT\n" +
            "  a.`document_name` AS DocName,\n" +
            "  a.`uuid` AS docUUID\n" +
            "FROM\n" +
            "  `sop_endorsed_sop_doc` a\n" +
            "  LEFT JOIN `sop_title` b\n" +
            "  ON a.`sop_id` = b.`id`\n" +
            "WHERE b.`dept_id` = ?1",nativeQuery = true)
    List<DocumentInterfaceViewDTO> getSopHistoryByFocal(Integer sec_id);

    @Query(value = "SELECT\n" +
            "  a.`document_name` AS DocName,\n" +
            "  a.`uuid` AS docUUID,\n" +
            "  a.`file_path` AS upload_URL\n" +
            "FROM\n" +
            "  `sop_endorsed_sop_doc` a\n" +
            "  LEFT JOIN `sop_title` b\n" +
            "  ON a.`sop_id` = b.`id`\n" +
            "WHERE a.`uuid` = ?1", nativeQuery = true)
    DocumentInterfaceViewDTO downloadEndorsedSop(String docUUID);
}
