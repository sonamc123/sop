package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sop_department")
public class DepartmentMaster {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String departmentShortCode;
	private String departmentName;
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
