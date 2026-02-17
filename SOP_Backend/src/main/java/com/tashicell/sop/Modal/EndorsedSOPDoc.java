package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sop_endorsed_Sop_Doc")
public class EndorsedSOPDoc {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @ManyToOne
    @JoinColumn(name = "sopId")
    private SopFileDetails sopId;
    private String document_type;
    private String document_name;
    private String uuid;
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "editedBy")
    private User editedBy;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
}
