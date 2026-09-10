package com.clinicadmin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheraphyProgramWithTheraphyNamesDto {
	
	private String id;
	private String programName;
	private int totalProgramAmount;
	private List<TheraphyNamesDTO> therophy;
	private String clinicId;
	private String branchId;
	private long theraphyCount;
	

}
