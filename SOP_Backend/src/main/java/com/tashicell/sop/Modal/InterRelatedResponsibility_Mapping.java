package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_inter_related_resp")
public class InterRelatedResponsibility_Mapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "resp_related_from")
    private Responsibilities respFrom;

    @ManyToOne
    @JoinColumn(name = "resp_related_to")
    private Responsibilities respTo;

    @ManyToOne
    @JoinColumn(name = "sec_related_to")
    private SectionMaster secTo;
}
