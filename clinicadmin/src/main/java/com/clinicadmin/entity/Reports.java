package com.clinicadmin.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reports {
	private String id;
	private String bookingId;
	private String patientId;
	private String customerMobileNumber;
	private String reportName;
	private String reportDate;
	private String reportStatus;
	private String reportType;

	private List<String>reportFile;
	 




}
