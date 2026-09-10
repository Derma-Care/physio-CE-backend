package com.dermacare.bookingService.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportsDTO {
	
	private String bookingId;
	private String customerMobileNumber;
	private String reportName;
	private String reportDate;
	private String reportStatus;
	private String reportType;
	private List<String> reportFile;
}