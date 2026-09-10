package com.clinicadmin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramWithTherophy {

	private String id;
	private String programName;
	private List<TherapyServiceDTO> therophyData;
	private String clinicId;
	private String branchId;
	private Integer totalTherophyIds;
}
