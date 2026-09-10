package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeVisitTrackingRequest {

    private String sittingsId;

    private String assignmentStatus;

    private OffsetDateTime assignedAt;

    private JourneyToPatientDTO journeyToPatient;

    private TreatmentResponse treatment;

    private JourneyToClinicDTO journeyToClinic;

    private CompletionDTO completion;
}