package com.tashicell.sop.Repository;

import com.tashicell.sop.Modal.InterRelatedResponsibility_Mapping;
import com.tashicell.sop.Record.SectionInterfaceDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Inter_Resp_Mapping_Repo extends JpaRepository<InterRelatedResponsibility_Mapping, Integer> {

//    @Query(value = "SELECT\n" +
//            "  a.`sec_related_to` AS secId\n" +
//            "FROM\n" +
//            "  `sop_inter_related_resp` a\n" +
//            "WHERE a.`resp_related_from` = ?1",nativeQuery = true)
//    List<SectionInterfaceDTO> getRelatedSecIdTo(Integer respId);

    @Query(value = "SELECT\n" +
            "  a.`sec_related_to` AS secId\n" +
            "FROM\n" +
            "  `sop_inter_related_resp` a\n" +
            "WHERE a.`resp_related_from` = ?1\n" +
            "  AND a.`sec_related_to` = ?2",nativeQuery = true)
    Integer getRelatedSecIdTo(Integer respId, Integer secId);


    @Modifying
    @Transactional
    @Query(value = "UPDATE\n" +
            "  `sop_inter_related_resp`\n" +
            "SET\n" +
            "  `resp_related_to` = ?1\n" +
            "WHERE `resp_related_from` = ?2 AND `sec_related_to` = ?3",nativeQuery = true)
    void updateInter_Resp_Mapping(Integer resp_related_to, Integer resp_related_from, Integer secId);


    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_inter_related_resp` a\n" +
            "WHERE a.`resp_related_from` = ?1",nativeQuery = true)
    InterRelatedResponsibility_Mapping getInterRespDtlsFrom(Integer respId);

    @Query(value = "SELECT\n" +
            "  *\n" +
            "FROM\n" +
            "  `sop_inter_related_resp` a\n" +
            "WHERE a.`resp_related_to` = ?1",nativeQuery = true)
    InterRelatedResponsibility_Mapping getInterRespDtlsTo(Integer respId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM `sop_inter_related_resp` WHERE `resp_related_from` = ?1",nativeQuery = true)
    void deleteInterRelatedRespFrm(Integer respId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM `sop_inter_related_resp` WHERE `resp_related_to` = ?1",nativeQuery = true)
    void deleteInterRelatedRespTo(Integer respId);
}
