package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_responsibilities")
public class Responsibilities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resId;

    @ManyToOne
    @JoinColumn(name = "sopId")
    private SopFileDetails sopId;

    @ManyToOne   
    @JoinColumn(name = "typeId")
    private SopTypeMaster typeId;

    @Column(columnDefinition = "LONGTEXT")
    private String remarks;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String responsibilityName;

    @ManyToOne
    @JoinColumn(name = "roleHolder")
    private DesignationMaster roleHolder;

    private LocalDateTime updatedOn;

    private LocalDateTime effectiveDate;

    private Character isEndorsed;
    private Character isAddendum;
    private Character wantToDelete = 'N';
    private String sopVersion;

    @ManyToOne
    @JoinColumn(name = "updatedBy")
    private User updatedBy;
}
