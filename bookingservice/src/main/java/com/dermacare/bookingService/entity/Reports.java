package com.dermacare.bookingService.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reports {
//   private String id;
	private String bookingId;
	private String customerMobileNumber;
	private String reportName;
	private String reportDate;
	private String reportStatus;
	private String reportType;
	private List<String> reportFile; 

}