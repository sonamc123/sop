package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sop_designation")
public class DesignationMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String designationName;

    @CreationTimestamp
    private LocalDateTime createdOn;
    private Integer status;

    @ManyToOne
    @JoinColumn(name = "createdBy")
    private User createdBy;

    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @ManyToOne
    @JoinColumn(name = "updatedBy")
    private User updatedBy;

    @ManyToOne
    @JoinColumn(name = "deptId")
    private DepartmentMaster deptId;
}
