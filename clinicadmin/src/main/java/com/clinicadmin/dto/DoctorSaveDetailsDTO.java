package com.clinicadmin.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorSaveDetailsDTO {
	private String id;
	private String patientId;
	private String doctorId;
	private String doctorName;
	private String clinicId;
	private String clinicName;
	private String customerId;
	private String bookingId;
	private String subServiceId;
	private LocalDateTime visitDateTime;
	private String visitType;
	private int visitCount; 
	private SymptomDetailsDTO symptoms;
	private TestDetailsDTO tests;
	private TreatmentResponseDTO treatments;
	private FollowUpDetailsDTO followUp;
	private PrescriptionDetailsDTO prescription;
	private List<String> prescriptionPdf;
	private LocalDateTime consultationStartDate;
    private LocalDateTime consultationExpiryDate;
    private String consultationType;
    // ✅ New sitting fields
    private int totalSittings;
    private int pendingSittings;
    private int takenSittings;
    private int currentSitting;
//  private int visitNumber;
}
