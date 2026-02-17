package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sop_file_log")
public class SopLogDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sopFileId")
    private SopFileDetails sopFileId;

    @Column(columnDefinition = "TEXT")
    private String action;

    @CreationTimestamp
    private LocalDateTime actionTime;

    @ManyToOne
    @JoinColumn(name = "actionTakenBy")
    private User actionTakenBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;


    private String sopVersion;

}
