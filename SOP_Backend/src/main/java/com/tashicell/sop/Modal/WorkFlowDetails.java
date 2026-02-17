package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_workflow_details")
public class WorkFlowDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "stageId")
	private StageMaster stageMaster;

	@ManyToOne
	@JoinColumn(name = "sopFileId")
	private SopFileDetails fileId;

	@Column(columnDefinition = "LONGTEXT")
	private String remarks;

	@ManyToOne
	@JoinColumn(name = "isEndorsed")

	private StageMaster isEndorsed;

	@CreationTimestamp
	private LocalDateTime createdOn;
	@ManyToOne
	@JoinColumn(name = "actionTakenBy")
	private User actionTakenBy;

	private LocalDateTime actionTakenOn;

	private String sopVersion;
}
