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
@Table(name = "sop_role_master")
public class RoleMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String roleName;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sop_role_privilege_mapping",
            joinColumns = {
                    @JoinColumn(name = "role_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "privilege_id", referencedColumnName = "id")
            }
    )
    private Collection<PriviledgeMaster> priviledgeMasters;

}
