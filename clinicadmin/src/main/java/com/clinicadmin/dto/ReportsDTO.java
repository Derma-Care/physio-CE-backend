package com.clinicadmin.dto;

import java.util.List;

import lombok.Data;

@Data
public class ReportsDTO {
	private String bookingId;
	private String patientId;
	private String customerMobileNumber;
	private String reportName;
	private String reportDate;
	private String reportStatus;
	private String reportType;
	private List<String> reportFile;

}
