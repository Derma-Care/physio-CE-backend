package com.clinicadmin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDtoList {
	
	private String id;
	private String customerId;
	private String patientId;
	private List<ReportsDTO> reportsList;
	

}