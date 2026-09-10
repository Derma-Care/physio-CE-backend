package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcerciseDTO {
	
	private String exerciseId;
	private String exerciseName;
	private Integer totalSessionCost;
	private Integer pricePerSession;
	private Integer noOfSessions;
	private Integer sets;
	private Integer repetitions;
	private Integer frequancy;
	private String notes;
	private String videoUrl;

}
