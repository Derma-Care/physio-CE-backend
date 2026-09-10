package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentDTO {
	private String id;
    private String title;
    private String description;
    private String status;
    private String raisedBy;
    private String assignedTo;
    private String priority;
    private String createdAt;
    private String updatedAt;
}
