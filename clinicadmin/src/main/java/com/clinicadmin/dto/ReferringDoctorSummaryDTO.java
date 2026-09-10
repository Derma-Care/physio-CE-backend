package com.clinicadmin.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferringDoctorSummaryDTO {

    private String doctorRefCode;
    private String referredByName;
    private Integer patientCount = 0;
    private Double revenue = 0.0;
    private List<ReferredPatientDTO> patients = new ArrayList<>();
}