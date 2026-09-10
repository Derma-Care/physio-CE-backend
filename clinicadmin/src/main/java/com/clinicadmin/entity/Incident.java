package com.clinicadmin.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.enumclasses.IncidentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

	@Id
	private String id;

	private String title;
	private String description;

	private IncidentStatus status; // NEW, IN_PROGRESS, RESOLVED, CLOSED

	private String raisedBy;
	private String assignedTo;
	private String priority; // LOW, MEDIUM, HIGH, CRITICAL
	private String createdAt;
	private String updatedAt;
}
