package com.dermacare.bookingService.dto;

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
	private String patientId;
	private Address patientAddress;
	private String age;
	private String gender;
	private String customerId;
	private String clinicId;

}
