package com.dermacare.bookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInfo {
	
		private String clinicId;
		private String branchId;
		private String patientName;
		private String date;
		private String doctorId;
		private String consultationFee;
		private String theraphyFee;
		private double finalAmount;
		private double dueAmount;
		private String consultationType;

}
