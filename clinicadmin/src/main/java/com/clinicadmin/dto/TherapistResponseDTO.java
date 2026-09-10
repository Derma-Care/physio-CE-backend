package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TherapistResponseDTO {
	private String clinicId;
	private String branchId;
	private String therapistId;
	private String fullName;
	private Integer yearsOfExperience;
	private String qualification;
	private List<String> expertiseAreas;
	private String contactNumber;
	private List<String> specializations;

}
