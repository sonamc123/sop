package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.Document;
import com.tashicell.sop.Record.DocumentInterfaceViewDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocuRepo extends JpaRepository<Document, Integer> {

    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_file_attachment` a\n" +
            "WHERE a.`uuid` = ?1", nativeQuery = true)
    Document getDocDtlsByUUID(String uuid);

    @Query(value = "SELECT\n" +
            "  doc.`uuid` AS docUUID,\n" +
            "  doc.`document_name` AS docName\n" +
            "FROM\n" +
            "  `sop_file_attachment` doc\n" +
            "  LEFT JOIN `sop_responsibilities` res\n" +
            "    ON doc.`sop_resp_id` = res.`res_id`\n" +
            "WHERE doc.`sop_resp_id` = ?1", nativeQuery = true)
    List<DocumentInterfaceViewDTO> getDocList(Integer resId);//

    @Query(value = "SELECT\n" +
            "  doc.`document_name` AS docName,\n" +
            "  doc.`document_type` AS document_Type,\n" +
            "  doc.`file_path` AS upload_URL,\n" +
            "  doc.`uuid` AS docUUID\n" +
            "FROM\n" +
            "  `sop_file_attachment` doc\n" +
            "WHERE doc.`uuid` = ?1", nativeQuery = true)
    DocumentInterfaceViewDTO DownloadDocByAuthor(String docUUID);


    @Modifying
    @Transactional
    @Query(value = "DELETE\n" +
            "FROM\n" +
            "  `sop_file_attachment`\n" +
            "WHERE `uuid` = ?1", nativeQuery = true)
    void deleteDocByUUID(String uuid);
}
