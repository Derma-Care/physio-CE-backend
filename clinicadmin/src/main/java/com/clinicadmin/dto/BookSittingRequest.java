package com.clinicadmin.dto;

import java.util.List;

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
public class BookSittingRequest {

    private String scheduleId;
    private String sittingsId;     // NEW — stable UUID, preferred lookup key
    private Integer sittingNumber; // kept for backward compatibility / fallback
    private String doctorId;
    private String patientId;
    private String condition;
    private String date;
    private String slot;
    private String bookingId;
    private List<PhysioMaxTreatedDoctor> treatedBy;
    private String treatmentPlan;
	private String visitType;
}