package com.tashicell.sop.Modal;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "sop_employee")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String firstName;
    private String middleName;
    private String lastName;
    private String mobileNo;
    private String emailId;
    private String password;
    private Character status;
    private Character gender;

    @ManyToOne
    @JoinColumn(name = "departmentId")
    private DepartmentMaster departmentMaster;

    @ManyToOne
    @JoinColumn(name = "sectionId")
    private SectionMaster sectionMaster;

    @ManyToOne
    @JoinColumn(name = "designation")
    private DesignationMaster designationMaster;

    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "updatedBy")
    private User updatedBy;

    private LocalDateTime updatedOn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(name = "sop_user_role_mapping",
            joinColumns = {
                    @JoinColumn(name = "empId", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "roleId", referencedColumnName = "id")
            }
    )

    private RoleMaster role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            // Add authorities based on role
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

            // Add authorities based on role's privileges
            Collection<PriviledgeMaster> privileges = role.getPriviledgeMasters();
            if (privileges != null) {
                for (PriviledgeMaster privilege : privileges) {
                    authorities.add(new SimpleGrantedAuthority(privilege.getPrivilegeName()));
                }
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
