package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_resp_sec_mapping")
public class Responsibility_Section_mapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer relID;

    @ManyToOne
    @JoinColumn(name = "respId")
    private Responsibilities responsibilities;

    @ManyToOne
    @JoinColumn(name = "deptId")
    private DepartmentMaster departmentMasterList;

    @ManyToOne
    @JoinColumn(name = "secId")
    private SectionMaster sectionMaster;

    private Character status;

    private String remarks;

}
