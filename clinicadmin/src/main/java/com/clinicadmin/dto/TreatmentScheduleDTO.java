package com.clinicadmin.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreatmentScheduleDTO {
	private String id;

	private String soapNoteId; // reference back to the originating SOAP note
	private String patientId;
	private String patientName;
	private String mobileNumber;
	private String clinicId;
	private String branchId;
	private String bookingId;
	private String doctorId;
	private String doctorName;
	private String complaints;
	private String packageType;
	private String packageId;
	private String packageName;
	private Double packagePrice;
	private Integer numberOfSittings;
	private String sessionStartDate;

	private List<Sittings> sittings;


}
