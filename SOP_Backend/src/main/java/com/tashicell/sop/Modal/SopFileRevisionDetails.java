package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_file_revision")
public class SopFileRevisionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sopFileId")
    private SopFileDetails fileId;

    @ManyToOne
    @JoinColumn(name = "revisedByUserId")
    private User revisedByUserId;

    @CreationTimestamp
    private LocalDate revisionDate;

    private String documentNo;

    @ManyToOne
    @JoinColumn(name = "departmentId")
    private DepartmentMaster departmentId;

    @ManyToOne
    @JoinColumn(name = "sectionId")
    private SectionMaster sectionId;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "statusId")
    private StageMaster statusId;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdOn;
    @ManyToOne
    @JoinColumn(name = "createdBy")
    private User user;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
    @ManyToOne
    @JoinColumn(name = "updatedBy")
    private User updatedBy;

}
