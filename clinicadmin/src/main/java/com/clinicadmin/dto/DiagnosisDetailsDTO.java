package com.clinicadmin.dto;

import java.util.List;

import lombok.Data;

@Data
public class DiagnosisDetailsDTO {

    private String vendorId;

    private String vendorName;

    private long totalPatients;

    // Status = Yes
    private long testedPatients;

    // Status = Pending
    private long pendingPatients;

    // Status = No
    private long notTestedPatients;

    private double yield;

    private List<DiagnosisPatientDTO> patients;
}