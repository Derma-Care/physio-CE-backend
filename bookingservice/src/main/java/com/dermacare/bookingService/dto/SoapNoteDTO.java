package com.dermacare.bookingService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class SoapNoteDTO {

	private String id;
	private String patientId;
	private String patientName;
	private String mobileNumber;
	private String clinicId;
	private String branchId;
	private String bookingId;
	private String doctorName;
	private String doctorId;
	private String complaints;
	private String packageType;
	private String packageId;
	private String packageName;
	private Integer numberOfSittings;
	private Double packagePrice;
	private String noteText;
	private String sessionStartDate;
	private String slot;
	// Incoming: base64-encoded content, or null — exactly as sent by frontend
	private String attachmentFileKey;
	private String audioNoteKey;

	private String createdAt;

}