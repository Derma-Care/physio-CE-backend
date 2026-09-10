package com.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingInfoByInput {
	
	private String name;
	private String relation;
	private String patientMobileNumber;
	private String mobileNumber;
	private String dob;
	private String patientId;
	private String patientAddress;
	private String age;
	private String gender;
	private String customerId;
	private String clinicId;
	private String branchId;
	private String email;

}
