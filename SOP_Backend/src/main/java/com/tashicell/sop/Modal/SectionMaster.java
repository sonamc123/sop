package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_section")
public class SectionMaster {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	private Integer id;
	private String sectionName;
	@ManyToOne
	private DepartmentMaster departmentId;

	private Integer status;

	@CreationTimestamp
	private LocalDateTime createdOn;

	@ManyToOne
	@JoinColumn(name = "createdBy")
	private User user;

	@UpdateTimestamp
	private LocalDateTime updatedOn;
	@ManyToOne
	@JoinColumn(name = "updatedBy")
	private User updatedBy;
}
