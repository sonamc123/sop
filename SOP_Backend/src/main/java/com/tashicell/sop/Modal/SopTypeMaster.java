package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "sop_type_master")
public class SopTypeMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer id;
    private String name;
    @CreationTimestamp
    private LocalDateTime createdOn;
    private Integer status;
    @ManyToOne
    @JoinColumn(name = "createdBy")
    private User user;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
    @ManyToOne
    @JoinColumn(name = "updatedBy")
    private User updatedBy;
}
