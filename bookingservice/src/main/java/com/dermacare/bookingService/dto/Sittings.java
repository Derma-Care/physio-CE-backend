package com.dermacare.bookingService.dto;

import java.util.List;

import com.dermacare.bookingService.dto.PhysioMaxTreatedDoctor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sittings {

	private String sittingsId; // unique id per sitting, generated on creation
	private Integer sittingNumber; // S1, S2, S3...
	private String doctorId;
	private String doctorName;
	private String date; // "dd/MM/yyyy" — null until booked
	private String slot; // time slot — null until booked
	private String condition; // mirrors parent SOAP note's complaints
	private String treatmentStatus; // "Not Started" | "In-progress" | "Completed"
	private String bookingId; // the specific booking tied to this sitting, null until booked
	private List<PhysioMaxTreatedDoctor> treatedBy;
}