package com.clinicadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorSummaryDTO {
    private String doctorId;
    private String doctorName;
    private Integer totalSittings;
    private Integer completedSittings;
    private Integer pendingSittings;
    private String providerType;
    // New field: list of patient summaries
    private List<PatientSummary> patientSummaries;
}
