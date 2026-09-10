package com.clinicadmin.entity;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.clinicadmin.dto.Sittings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "treatment_schedules")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreatmentSchedule {

	@Id
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
	private Double pricePerSitting;
	private String sessionStartDate;

	private List<Sittings> sittings;

	private Instant createdAt;
	private Instant updatedAt;
}