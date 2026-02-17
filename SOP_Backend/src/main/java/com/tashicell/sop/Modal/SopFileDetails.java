package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "sop_title")
public class SopFileDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "updateType")
    private SopTypeMaster sopTypeMaster;

    private String sopVersion;

    private String addendumReason;

    @ManyToOne
    @JoinColumn(name = "deptId")
    private DepartmentMaster deptID;

    @ManyToOne
    @JoinColumn(name = "secId")
    private SectionMaster secID;

    @Column(columnDefinition = "LONGTEXT")
    private String introduction;

    @ManyToOne
    @JoinColumn(name = "createdBy")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
    @ManyToOne
    @JoinColumn(name = "updatedBy")
    private User updatedBy;



}
