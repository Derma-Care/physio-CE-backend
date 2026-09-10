package com.clinicadmin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherophyDataDto {

	private String id;
	private String therapyName;
	private Integer therapyCost;
	private Integer noOfSessionCount;
	private Integer noExerciseIdCount;
	private List<ExcerciseDTO> exercises;
}
