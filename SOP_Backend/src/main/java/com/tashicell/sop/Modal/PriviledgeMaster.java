package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_privilege_master")
public class PriviledgeMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String privilegeName;

    @ManyToMany(mappedBy = "priviledgeMasters", fetch = FetchType.LAZY)
    private Collection<RoleMaster> role;

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
