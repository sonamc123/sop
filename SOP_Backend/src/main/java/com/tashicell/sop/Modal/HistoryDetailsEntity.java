package com.tashicell.sop.Modal;

import com.tashicell.sop.Repository.ChangeHistoryRepo;
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
@Table(name = "sop_historyDtls")
public class HistoryDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sopType")
    private SopTypeMaster sopType;

    private Integer sopNo;
    private LocalDateTime effectiveDate;
    private String significantChanges;
    private String previousSopNo;
    private String currentSopNo;
    private Character isEndorsed;
    private Character isAddendum;


}
