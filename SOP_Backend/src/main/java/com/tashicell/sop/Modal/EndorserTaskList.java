package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_endorser_TaskList")
public class EndorserTaskList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "stageId")
    private StageMaster stageMaster;

    @ManyToOne
    @JoinColumn(name = "sopFileId")
    private SopFileDetails sopId;

    @Column(columnDefinition = "LONGTEXT")
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "endorserId")
    private User endorserId;

    private String reviewerId;

    private LocalDateTime actionTakenOn;

    private String sopVersion;

}
