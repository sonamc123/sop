package com.tashicell.sop.Modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sop_file_attachment")
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer Id;

	@ManyToOne
	@JoinColumn(name = "sopRespId")

	private Responsibilities respId;
	@Column(columnDefinition = "LONGTEXT")

	private String document_type;
	@Column(columnDefinition = "LONGTEXT")

	private String document_name;
	@Column(columnDefinition = "LONGTEXT")
	private String uuid;
	private String filePath;

	@ManyToOne
	@JoinColumn(name = "editedBy")
	private User editedBy;

	@UpdateTimestamp
	private LocalDateTime updatedOn;
}
